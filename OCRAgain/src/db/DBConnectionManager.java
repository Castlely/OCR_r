package db;
import java.io.*;

import java.sql.*;
import java.util.*;
import java.util.Date; 


/**
 * 管理类DBConnectionManager支持对一个或多个由属性文件定义的数据库连接
 *池的访问.客户程序可以调用getInstance()方法访问本类的唯一实例.
 * 
 *       
 */  
public class DBConnectionManager {
	static private DBConnectionManager instance; // 唯一实例
	static private int clients;
	static public int sms = 0;
	private Vector drivers = new Vector();
	private PrintWriter log;
	private Hashtable pools = new Hashtable();

	
	private int maxConn ;
	private String name;
	private String password;
	private String URL; 
	private String user; 
	private String driverClasses ; 
	private int waraConnCount;
	private int timeout;
	
	/**
	 * 返回唯一实例.如果是第一次调用此方法,则创建实例
	 * @return DBConnectionManager 唯一实例
	 */ 
	static synchronized public DBConnectionManager getInstance() {
		if (instance == null) {
			instance = new DBConnectionManager();
		}
		clients++;
		return instance;
	}
	/**
	 * 建构函数私有以防止其它对象创建本类实例
	 */
	private DBConnectionManager() {
		init();
	}
	/**
	 * * 将连接对象返回给由名字指定的连接池
	 * @param name在属性文件中定义的连接池名字
	 * @param con连接对象   
	 */

	public  void  freeConnection(String name, Connection con) {
		DBConnectionPool pool = (DBConnectionPool) pools.get(name);
		if (pool != null) {
			pool.freeConnection(con);
		}
	}
	/**
	 * 获得一个可用的(空闲的)连接.如果没有可用连接,且已有连接数小于最大连接数限制,则创建并返回新连
	 * @param name在属性文件中定义的连接池名字 
	 * @return Connection 可用连接或null 
	 */
	public Connection getConnection(String name) {
		DBConnectionPool pool = (DBConnectionPool) pools.get(name);
		if (pool != null) {
			return pool.getConnection();
		}
		return null;
	}

	public Connection getConnection_v(String name) {
		DBConnectionPool pool = (DBConnectionPool) pools.get(name);
		if (pool != null) {
			return pool.getConnection_v();
		}
		return null;
	}




	/**
	 * 关闭所有连接,撤销驱动程序的注册
	 */
	public synchronized void release() {
		// 等待直到最后一个客户程序调用
		if (--clients != 0) {
			return;
		}
		Enumeration allPools = pools.elements();
		while (allPools.hasMoreElements()) {
			DBConnectionPool pool = (DBConnectionPool) allPools.nextElement();
			pool.release();
		}
		Enumeration allDrivers = drivers.elements();
		while (allDrivers.hasMoreElements()) {
			Driver driver = (Driver) allDrivers.nextElement();
			try {
				DriverManager.deregisterDriver(driver);
				System.out.println("撤销JDBC驱动程序 " + driver.getClass().getName() + "的注册");
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println( "无法撤销下列JDBC驱动程序的注册: " + driver.getClass().getName());
			}
		}
	}
	/**
	 * 根据指定属性创建连接池实例.
	 * @param props 连接池属性
	 */
	private void createPools() {
		DBConnectionPool pool = new DBConnectionPool(); 
		pools.put("db", pool);
	} 
	/**
	 * 读取属性完成初始化
	 */
	private void init() { 
		if(loadConfig()){
			loadDrivers();
			createPools();
		}
	} 
	/**
	 * 读取数据库配置
	 */
	private boolean loadConfig(){
		boolean b=false;
		Properties propertie = new Properties();
	        try {
	          //该文件的路劲在classes根路径下   
	            propertie.load(getClass().getResourceAsStream("/db.properties"));   
	            try{
	            	//读取数据库配置
		             name = propertie.getProperty("dbname").trim();
		        	 maxConn = Integer.parseInt(propertie.getProperty("db.maxconn").trim());
		        	 user =  propertie.getProperty("db.user").trim();
		        	 password = propertie.getProperty("db.password").trim();
		        	 URL = propertie.getProperty("db.url").trim();  
		        	 driverClasses = propertie.getProperty("drivers").trim();
		        	 waraConnCount=Integer.parseInt(propertie.getProperty("waraConnCount").trim());
		        	 timeout=Integer.parseInt(propertie.getProperty("db.timeout").trim()); 
	            }catch(Exception e){
	            }
	        	 b=true;
	        	// logger.info("装载数据库配置文件成功!");
	        } catch (FileNotFoundException ex){
	        	System.out.println("读取属性文件--->失败！- 原因：文件路径错误或者文件不存在="+ex);
	        } catch (IOException ex){
	        	System.out.println("装载文件--->失败!="+ex);
	        }
	        return b;
	}
	/**
	 * 装载和注册所有JDBC驱动程序
	 * @param props属性
	 */
	private void loadDrivers() {
	//	String driverClasses = "oracle.jdbc.driver.OracleDriver"; 
		StringTokenizer st = new StringTokenizer(driverClasses);
		while (st.hasMoreElements()) {
			String driverClassName = st.nextToken().trim(); 
			try {
				Driver driver = (Driver) Class.forName(driverClassName).newInstance();
				DriverManager.registerDriver(driver);
				drivers.addElement(driver);
				//System.err.println("成功注册JDBC驱动程序" + driverClassName);
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 将文本信息写入日志文件
	 */
	private void log(String msg) {
		log.println(new Date() + ": " + msg); 
	}
	/**
	 * 将文本信息与异常写入日志文件       
	 */
	private void log(Throwable e, String msg) {
		log.println(new Date() + ": " + msg);
		e.printStackTrace(log);
	}
	/**
	 * 此内部类定义了一个连接池.它能够根据要求创建新连接,直到预定的最大值
	 *
	 *
	 */
	class DBConnectionPool {

		
		private int checkedOut=0;
		private Vector freeConnections = new Vector();
		
		/**
		 * 创建新的连接池
		 * @param name连接池名字
		 * @param URL数据库的JDBC URL
		 * @param user数据库帐号,或 null
		 * @param password密码,或 null
		 * @param maxConn此连接池允许建立的最大连接数
		 */
		public DBConnectionPool() {
		}

		/**
		 * 将不再使用的连接返回给连接池
		 * @param con客户程序释放的连接
		 */
		public synchronized void freeConnection(Connection con) {
			// 将指定连接加入到向量末尾
			//logger.info("...free......"+con+"....freeCon="+(30-checkedOut));
			freeConnections.addElement(con);
			checkedOut--;
			notifyAll();
		}

		/**
		 * 从连接池获得一个可用连接.如没有空闲的连接且当前连接数小于最大连接 数限制,则创建新连接.
		 * 如原来登记为可用的连接不再有效,则从向量删除之,
		 * 然后递归调用自己以尝试新的可用连接.
		 */
		public synchronized Connection getConnection() {
			Connection con = null;
			if (freeConnections.size() > 0) {// 获取向量中第一个可用连接
				con = (Connection) freeConnections.firstElement();
				freeConnections.removeElementAt(0);
				try {    
					if (con.isClosed()) {
						// 递归调用自己,尝试再次获取可用连接
						con = getConnection();
					}  
				} catch (SQLException e) {
					// 递归调用自己,尝试再次获取可用连接
					con = getConnection();
				}  
			}
			else if (maxConn == 0 || checkedOut < maxConn) {
				con = newConnection();
			}
			if (con != null) {
				checkedOut++;
			}
			/*连接池连接数=20个的时候  系统警告以短信形式发送*/
			if(checkedOut == waraConnCount&&sms==0){
				try {
					sms=1;
					//处理
				} catch (Exception e) {
					e.printStackTrace();
				}
			}  
			/* *********          end        ************** */
			
			//logger.info("...getcon......"+con+"....applyCon="+checkedOut);
			return con;
		} 

		public synchronized Connection getConnection_v() {
			long startTime = new Date().getTime();
			Connection con;
			if ((con = getConnection()) == null) {
				try {
					wait(timeout);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if((con = getConnection()) == null){ //等待5秒以后还未有连接 重新建立连接池
					release();
					instance = null;
					clients=0;
				}    
			}   
			return con;
		}


		/**
		 * 从连接池获取可用连接.可以指定客户程序能够等待的最长时间 参见前一个getConnection()方法.
		 * @param timeout以毫秒计的等待时间限制
		 */
		public synchronized Connection getConnection(long timeout) {
			long startTime = new Date().getTime();
			Connection con;
			while ((con = getConnection()) == null) {
				try {
					wait(timeout);
				} catch (InterruptedException e) {
					System.out.println(e.getMessage());
				}
				if ((new Date().getTime() - startTime) >= timeout) {// wait()返回的原因是超时
					return null;
				}
			}
			return con;
		}

		/**
		 * 关闭所有连接
		 */
		public synchronized void release() {
			Enumeration allConnections = freeConnections.elements();
			while (allConnections.hasMoreElements()) {
				Connection con = (Connection) allConnections.nextElement();
				try {
					con.close();
					System.out.println("关闭连接池" + name + "中的一个连接");
				} catch (SQLException e) {
					e.printStackTrace();
					log(e, "无法关闭连接池" + name + "中的连接");
					e.printStackTrace();
				}
			}
			freeConnections.removeAllElements();
		}

		/**
		 * 创建新的连接
		 */
		private Connection newConnection() {
			Connection con = null;
			try {
				if (user==null||"".equals(user)) {
					con = DriverManager.getConnection(URL);
				} else {
					con = DriverManager.getConnection(URL, user, password);
				}
			} catch (SQLException e) {
				return null;
			}
			return con;
		}
	} 
}
