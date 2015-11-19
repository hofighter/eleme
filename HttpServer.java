import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
//json
import net.sf.json.JSONObject;




public class HttpServer {
	
	static String drivername = "com.mysql.jdbc.Driver";
	static String url = "jdbc:mysql://127.0.0.1:3306/eleme";
	static String username = "root";
	static String password = "toor";
	
	static{
		try{
			Class.forName(drivername);
		}catch(ClassNotFoundException e){
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		Connection connection = null;
		Statement statement = null;

		try {
			connection = DriverManager.getConnection(url,username,password);
			statement = (Statement)connection.createStatement();
			
			String drop_1 = "drop table if exists token";
			String drop_2 = "drop table if exists token_cart";
			String drop_3 = "drop table if exists cart";
			String drop_4 = "drop table if exists cart_order";
			statement.executeUpdate(drop_1);
			statement.executeUpdate(drop_2);
			statement.executeUpdate(drop_3);
			statement.executeUpdate(drop_4);
			
			String sql_1 = "create table if not exists token ( id int(11), access_token varchar(32), iforder char(1))";
			String sql_2 = "create table if not exists token_cart (access_token varchar(32),cart_id varchar(32),count int(11))";
			String sql_3 = "create table if not exists cart (cart_id varchar(32),food_id varchar(32),count int(11))";
			String sql_4 = "create table if not exists cart_order (cart_id varchar(32),order_id varchar(32),total int(11))";
			statement.executeUpdate(sql_1);
			statement.executeUpdate(sql_2);
			statement.executeUpdate(sql_3);
			statement.executeUpdate(sql_4);
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			try {
				if(connection != null) connection.close();
				if(statement != null) statement.close();
			}catch(SQLException e1) {
				e1.printStackTrace();
			}
		}
		
		try {
			serverSocket = new ServerSocket(8080);
			while (true) {
				Socket client = serverSocket.accept();
				new Thread(new Handler(client)).start();
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				if (serverSocket != null)
					serverSocket.close();
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	private static class Handler implements Runnable {

		private Socket socket;
		private BufferedReader in;
		private FileWriter wOut;
		private int cLength;
		private String content;
		private OutputStream out;
		private JSONObject js;
		
		
		
		private Connection conn = null ;//= JDBC_Connection.getConnection();
		private ResultSet rs = null;
		private Statement stmt = null;
		private PreparedStatement ps = null; 
		
		

		Handler(Socket socket) {
			this.socket = socket;
			this.content = null;
			
			try {
				in = new BufferedReader(new InputStreamReader(this.socket.getInputStream(),"utf-8"));
				wOut = new FileWriter("log.txt",true);
				out = socket.getOutputStream();
				cLength = 0;
				content = null;
				conn = DriverManager.getConnection(url,username,password); 
			}catch (IOException e) {
				System.out.println(e.getMessage());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	
		
		
		private void getParameter() {
			char[] text = new char[1024*1024];
			try {
				
				while(in.read(text) != -1) {
					String[] everyLine = String.valueOf(text).toLowerCase().split("\r\n");
					int i;
					for(i = 0; i < everyLine.length; ++i) {
						if(everyLine[i].indexOf("content-length") != -1) {
							String[] str = everyLine[i].split(":");
							cLength = Integer.valueOf(str[1].trim());
							if(cLength == 0) break;
						}
						if(cLength != 0) {
							if(everyLine[i].equals("") && everyLine.length > i + 1) break;
						}
					}
					if(cLength == 0) break;
					if(everyLine.length > i + 1) {
						content = everyLine[i + 1];
						return;
					}
				}
				//System.out.println(cLength + content);
    		}catch (IOException e) {
				System.out.println(e.getMessage());
			}
	    }
		
	
		private String getId() {
			char[] token = new char[32];
			token = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
			Random r = new Random();
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < 32; ++i) {
				sb.append(token[r.nextInt(33)]);
			}
			return sb.toString();
		}

	
	    private void login() {
	    	getParameter();
	    	
	    	if(cLength == 0) {
		    	String body = "{\"code\": \"EMPTY_REQUEST\",\"message\": \"请求体为空\"}";
				StringBuilder sb_1 = new StringBuilder();
				sb_1.append("HTTP/1.1 400 Bad request\r\n");
				sb_1.append("Content-Type: application/json; charset=utf-8\r\nContent-Length: ");
				try {
					sb_1.append(String.valueOf(body.getBytes("utf-8").length));
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				sb_1.append("\r\n\r\n");
				sb_1.append(body);
				try {
					out.write(sb_1.toString().getBytes("utf-8"));
					out.flush();
					return;
				}catch(IOException e) {
					System.out.println(e.getMessage());
				}
	    	}
	    	try {
	    		js = JSONObject.fromObject(content);
	    	}catch(Exception e) {
	    		String body = "{\"code\": \"MALFORMED_JSON\",\"message\": \"格式错误\"}";
				StringBuilder sb_1 = new StringBuilder();
				sb_1.append("HTTP/1.1 400 Bad request\r\n");
				sb_1.append("Content-Type: application/json; charset=utf-8\r\nContent-Length: ");
				try {
					sb_1.append(String.valueOf(body.getBytes("utf-8").length));
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				sb_1.append("\r\n\r\n");
				sb_1.append(body);
				try {
					out.write(sb_1.toString().getBytes("utf-8"));
					out.flush();
					return;
				}catch(IOException ex) {
					System.out.println(ex.getMessage());
				}
	    	}
	    
	    	String userName = js.getString("username");
	    	String passWord = js.getString("password");
	    	
	    	String sql = "select id,name,password from user where name = ? and password = ?";
	    	try {
				ps = (PreparedStatement)conn.prepareStatement(sql);
				ps.setString(1, userName);
				ps.setString(2, passWord);
				rs = ps.executeQuery();
				while(rs.next()) {
				
					String accessToken = getId();
					String sql_insert = "insert into token(id,access_token,iforder) values(?,?,'0')";
					ps = (PreparedStatement)conn.prepareStatement(sql_insert);
					int user_id = rs.getInt("id");
					ps.setInt(1, user_id);
					ps.setString(2, accessToken);
				    ps.executeUpdate();
				 
				    String body = "{\"user_id\": " + user_id + ",\"username\": \"" + userName + "\",\"access_token\": \""+ accessToken +"\"}";
					StringBuilder sb_1 = new StringBuilder();
					sb_1.append("HTTP/1.1 200 OK\r\n");
					sb_1.append("Content-Type: application/json; charset=utf-8\r\nContent-Length: ");
					sb_1.append(String.valueOf(body.getBytes("utf-8").length));
					sb_1.append("\r\n\r\n");
					sb_1.append(body);
					try {
						//System.out.println(sb_1.toString());
						out.write(sb_1.toString().getBytes("utf-8"));
						out.flush();
						return;
					}catch(IOException ex) {
						System.out.println(ex.getMessage());
					}
										
				}
			
				String body = "{\"code\": \"USER_AUTH_FAIL\",\"message\": \"用户名或密码错误\"}";
				StringBuilder sb_1 = new StringBuilder();
				sb_1.append("HTTP/1.1 403 Forbidden\r\n");
				sb_1.append("Content-Type: application/json; charset=utf-8\r\nContent-Length: ");
				sb_1.append(String.valueOf(body.getBytes("utf-8").length));
				sb_1.append("\r\n\r\n");
				sb_1.append(body);
				try {
					//System.out.println(sb_1.toString());
					out.write(sb_1.toString().getBytes("utf-8"));
					out.flush();
					return;
				}catch(IOException ex) {
					System.out.println(ex.getMessage());
				}
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
	   
	    private void foods() {
	    	
	    }
	    
	   
	    private void carts() {
	    	
	    }
	    
	    //...
	   

		@Override
		public void run() {
			try {
				StringBuilder sb = new StringBuilder();
				while(true) {
					
					int temp = in.read();
					if(temp == -1 || (char)temp == '\r' || (char)temp == '\n') break;
					sb.append((char)temp);
				}
				String[] contents = sb.toString().split(" ");
				if(contents[0].equals("GET")) {
					
					if(contents[1].equals("/foods")) {
						foods();
					}
					//...
				}
				if(contents[0].equals("POST")) {
				
					if(contents[1].equals("/login")) {
						login();
					}
					if(contents[1].equals("/carts")) {
						carts();
					}
					//...
					
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}  finally {
				if (socket != null) {
					try {
						socket.close();
						in.close();
						wOut.close();
					} catch (IOException e) {
						System.out.println(e.getMessage());
					}
				}
				try{
					if(rs != null){
						rs.close();
					}
				}catch (SQLException e){
					e.printStackTrace();
				}try{
					if(conn != null){
					   conn.close();
					}
					}catch(SQLException e){
						e.printStackTrace();
					}try{
						if(stmt != null)
							stmt.close();
						if(ps != null)
							ps.close();
						}catch(SQLException e){
							e.printStackTrace();
					}
			}

		}

	}

}
