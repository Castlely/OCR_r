package db;

import java.sql.CallableStatement;




import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

public class Db {
	private static HashMap<String, HashMap<String, ArrayList<HashMap<String, String>>>> Pref = new HashMap<String, HashMap<String, ArrayList<HashMap<String, String>>>>();
	private DBConnectionManager connMgr = null;
	private ResultSet rs = null;
	private CallableStatement cst = null;
	private PreparedStatement ps = null;
	private Connection cn = null;
	public HashMap<String, String> ParaMap = new HashMap<String, String>();
	public HashMap<String, Object> OutMap = new HashMap<String, Object>();

	public Db() {;
		connMgr = DBConnectionManager.getInstance();
	}

	public Connection getConnection() {
		this.cn = connMgr.getConnection("db");
		return this.cn;
	}
	/**
	 * 执行SQL
	 * @param Sql
	 * @param BindMap
	 * @return
	 */
	public boolean execute(String Sql, HashMap BindMap){
		boolean b=false;
		Pattern p = Pattern.compile("\\[.+?\\]");
		String ReplacedSql = Sql.replaceAll("\\[.+?\\]", "?");
		cn = this.getConnection();
		try {
			ps = cn.prepareStatement(ReplacedSql);
			Matcher m = p.matcher(Sql);
			int s = 0;
			while (m.find()) {
				++s;
				String key=m.group(0).replace("[", "").replace("]", "").toUpperCase();
				//String obj=((String[])BindMap.get(key))[0].trim();
				Object obj = BindMap.get(key);
				ps.setObject(s, obj);
			}
			//b=ps.execute();
			if(ps.executeUpdate()>0){
				b=true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			this.free();
		}
		return b;
	}
	/**
	 * 
	 * @param Sql
	 * @param BindMap
	 * @return
	 */
	public JSONArray getJSON(String Sql, HashMap BindMap) {
		JSONArray members = new JSONArray();
		Pattern p = Pattern.compile("\\[.+?\\]");
		String ReplacedSql = Sql.replaceAll("\\[.+?\\]", "?");
		cn = this.getConnection();
		try
		{
			ps = cn.prepareStatement(ReplacedSql);
			Matcher m = p.matcher(Sql);
			int s = 0;
			while (m.find()) {
				++s;
				String key=m.group(0).replace("[", "").replace("]", "").toUpperCase();
				//String obj=((String[])BindMap.get(key))[0].trim();
				Object obj = BindMap.get(key);
				ps.setObject(s, obj);
			}
 			ResultSet rs = null;
			rs = ps.executeQuery();
			int j=0;
			while (rs.next()) {
				JSONObject member = new JSONObject();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); ++i){
					if (rs.getObject(i) != null) {
						member.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
					}
					else{
						member.put(rs.getMetaData().getColumnName(i), "");
					}
					
				}

				members.put(j, member);
				j++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.free();
		}finally{
			this.free();
		}
		return members;
	}
	/**
	 * 
	 * @param Sql
	 * @param BindMap
	 * @return
	 */
	public String getXML(String Sql, HashMap BindMap) {
		StringBuffer xml = new StringBuffer();
		Pattern p = Pattern.compile("\\[.+?\\]");
		String ReplacedSql = Sql.replaceAll("\\[.+?\\]", "?");
		cn = this.getConnection();
		try
		{
			ps = cn.prepareStatement(ReplacedSql);
			Matcher m = p.matcher(Sql);
			int s = 0;
			while (m.find()) {
				++s;
				String key=m.group(0).replace("[", "").replace("]", "").toUpperCase();
				//String obj=((String[])BindMap.get(key))[0].trim();
				Object obj = BindMap.get(key);
				ps.setObject(s, obj);
			}
 			ResultSet rs = null;
			rs = ps.executeQuery();
			int j=0;
			xml.append("<ROOT>");
			while (rs.next()) {
				xml.append("<OBJECT>");
				j++;
				xml.append("<SEQ>"+j+"</SEQ>");
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); ++i){
					String lable=rs.getMetaData().getColumnName(i);
					if (rs.getObject(i) != null) {
						xml.append("<"+lable+">"+rs.getObject(i)+"</"+lable+">");
					}
					else{
						xml.append("<"+lable+"></"+lable+">");
					}
					
				}
				xml.append("</OBJECT>");
			}
			xml.append("</ROOT>");
		} catch (Exception e) {
			e.printStackTrace();
			this.free();
		}finally{
			this.free();
		}
		return xml.toString();
	}
	public List getLikeList(String Sql, HashMap BindMap) {
		cn = this.getConnection();
		List list = new ArrayList();
		try {
			ps = cn.prepareStatement(Sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				HashMap RowMap = new HashMap();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); ++i)
					if (rs.getObject(i) != null) {
						RowMap.put(rs.getMetaData().getColumnName(i), rs
								.getObject(i));
					} else
						RowMap.put(rs.getMetaData().getColumnName(i), null);
				list.add(RowMap);
			}
			this.free();
			return list;
		} catch (Exception e) {
			this.free();
		}
		return null;
	}
	
	public HashMap getHashMap(String Sql, HashMap BindMap) {
		cn = this.getConnection();
		HashMap RowMap = new HashMap();
		try {
			ps = cn.prepareStatement(Sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				String key=(String) rs.getObject(1);
				String value=(String) rs.getObject(2);
				RowMap.put(key.toUpperCase(), value);
			}
			this.free();
		} catch (Exception e) {
			this.free();
		}
		return RowMap;
	}
	
	public int executeUpdate(String strSQL) {
		int i = 0;
		try {
			this.cn = connMgr.getConnection("db");
			this.ps = this.cn.prepareStatement(strSQL);
			i = this.ps.executeUpdate();
		} catch (Exception e) {
		} finally {
			this.free();
		}
		return i;
	}

	public void free() {
		connMgr.freeConnection("db", this.cn);
		try {
			if (this.rs != null) {
				this.rs.close();
			}
		} catch (SQLException e) {
			System.out.println("数据DB释放错误 关闭 RS="+ e);
		}

		try {
			if (this.ps != null) {
				this.ps.close();
			}
		} catch (SQLException e) {
			System.out.println("数据DB释放错误 关闭 PS"+e);
		}

		try {
			if (this.cst != null) {
				this.cst.close();
			}
		} catch (SQLException e) {
			System.out.println("数据DB释放错误 关闭 CST"+e);
		}
	}

	

}