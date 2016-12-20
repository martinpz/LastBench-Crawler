/*
* Copyright (C) 2014 University of Freiburg.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package db;

import runner.HF;
import db.SQL;

public class Lib {
	
	static String cTable = "CREATE TABLE %s (%s);";
	
	static final String rel_core_parm = "%1$s INT(25), %2$s INT(25)"
									  + ", ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
									  + ", PRIMARY KEY (%1$s, %2$s)";

	
	public static int insert(String tableName, String rows, String values){
		
		String row_sql = "INSERT INTO %s (%s) VALUES (%s)";
		
		row_sql = String.format(row_sql, tableName, rows, values);
		
//		HF.print(row_sql);
		
		return SQL.exe_insert(row_sql);
	}

	public static int simpleInsert(String tableName, String recordName){
		
		String row_sql = "INSERT INTO %s (%s) VALUES (%s)";
		
		recordName = clean(recordName);
		
		row_sql = String.format(row_sql, tableName, "name", String.format("'%s'", recordName));
		
//		HF.print(row_sql);
		
		return SQL.exe_insert(row_sql);
	}

	public static String clean(String word){
		try {
			return word.replaceAll("'", "__");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return word;
		}
	}
	
	public static String unclean(String word){
		return word.replaceAll("__", "'");
	}
	
	public static int insert(String coockedQuery){
		return -1;
	}
	
	//#TODO implement this for the relation tuples
//	public static boolean check_exsits(String tableName, String recordName){	
//	}
	
	public static boolean check_exsits(String tableName, String recordName){
		
		String check_stmt = null;

		int db_status;
		
		recordName = clean(recordName);
		
		check_stmt = "SELECT EXISTS(SELECT 1 FROM %s WHERE name='%s') as id";
		
		check_stmt = String.format(check_stmt, tableName, recordName);
				
		db_status = SQL.exe_select(check_stmt);
		
		if(db_status == 1){
			return true;
		}
				
		return false;
	}
	
	public static boolean check_exsits_explored(String tableName, String recordName){
		
		String check_stmt = null;

		int db_status;
		
		recordName = clean(recordName);
		
		check_stmt = "SELECT EXISTS(SELECT 1 FROM %s WHERE name='%s'AND visited = '2') as id";
		
		check_stmt = String.format(check_stmt, tableName, recordName);
				
		db_status = SQL.exe_select(check_stmt);
		
		if(db_status == 1){
			return true;
		}
				
		return false;
	}
	
	public static boolean check_exsits(int recordId){
		return false;
	}
	
	public static int get_id(String tableName, String recordName){
		
		String row_sql = "SELECT id FROM %s WHERE name='%s'";
		
		recordName = clean(recordName);
		
		row_sql = String.format(row_sql, tableName, recordName);
				
		if (check_exsits(tableName, recordName)){
			
			return db.SQL.exe_select(row_sql);
			
		}
		
		return -1;
	}
	
	public static String get_name(String tableName, int recordId){
		
		String row_sql = "SELECT name FROM %s WHERE id='%s'";
		
		row_sql = String.format(row_sql, tableName, recordId);
				
		return db.SQL.exe_select_string(row_sql);
	}
	
	public static void update(String tableName, String content_stmt, String recordName){
		
		String row_stmt = "UPDATE %s SET %s WHERE name = '%s';";
		
		row_stmt = String.format(row_stmt, tableName, content_stmt, recordName);
		
//		HF.print(row_stmt);
		
		SQL.exe_insert(row_stmt);
	}
	
	
	public static void update_to_seen(String tableName, String recordName){
		
		String content_stmt = "visited = '1'";
		
		String row_stmt = "UPDATE %s SET %s WHERE name = '%s';";
		
		row_stmt = String.format(row_stmt, tableName, content_stmt, recordName);
		
		//HF.print(row_stmt);
		
		SQL.exe_insert(row_stmt);
	}
	
	public static void update_to_completed(String tableName, String recordName){
		
		String content_stmt = "visited = '2'";
		
		String row_stmt = "UPDATE %s SET %s WHERE name = '%s';";
		
		row_stmt = String.format(row_stmt, tableName, content_stmt, recordName);
		
		//HF.print(row_stmt);
		
		SQL.exe_insert(row_stmt);
	}
	
	
	// creating a relationship table
	public static void cRelTable(String relName, String obj1Name,
				String obj2Name) {

			String _relTable = "", _rel_core_parm = "";

			obj1Name += "_id";
			obj2Name += "_id";

			if (relName == "") {
				relName = String.format("%s_%s", obj1Name, obj2Name);
			}

			_rel_core_parm = String.format(rel_core_parm, obj1Name, obj2Name);
			_relTable = String.format(cTable, relName, _rel_core_parm);

//			HF.print(_relTable);

			SQL.exe_insert(_relTable);
		}
}
