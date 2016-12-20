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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import runner.HF;

/**
 * All SQL statements related to execution at the database side to be handled at
 * this class
 * 
 * All the prepared statements and result set have been set to null
 * in order for java to clean its statements
 * 
 * @version: 0.1
 ***/
public class SQL {

	/**
	 * input: cooked_sql update_execute() return: id of the record inserted into
	 * the database
	 ***/
	public static int exe_insert(String sql) {

		int _id = -1;

		PreparedStatement st;

		try {

			st = Jdbc.getConn().prepareStatement(sql);

			st.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

			ResultSet rs = st.getGeneratedKeys();

			if (rs.next()) {
				_id = rs.getInt(1);
			}

//			 HF.print(String.format("returned id is: %s", _id));

			rs = null;
			
		} catch (SQLException e) {
//			HF.print("SQL ERROR :: " + e);
//			log.System.add_to_log("SQL ERROR :: " + e);
		}
		
		st = null;
		return _id;
	}

	public static int exe_select(String sql) {

		int _id = -1;

		PreparedStatement st;

		try {

			st = Jdbc.getConn().prepareStatement(sql);

			ResultSet rs = st.executeQuery();

			if (rs.next()) {
				_id = rs.getInt(1);
			}

			// HF.print(String.format("returned id is: %s", _id));

			rs = null;
		} catch (SQLException e) {
//			HF.print("SQL ERROR :: " + e);
//			log.System.add_to_log("SQL ERROR :: " + e);
		}
		
		st = null;

		return _id;
	}

	public static String exe_select_string(String sql) {

		String _name = null;

		PreparedStatement st;

		try {

			st = Jdbc.getConn().prepareStatement(sql);

			ResultSet rs = st.executeQuery();

			if (rs.next()) {
				_name = rs.getString(1);
			}

			// HF.print(String.format("returned id is: %s", _id));
			
			rs = null;
			
		} catch (SQLException e) {
//			HF.print("SQL ERROR :: " + e);
//			log.System.add_to_log("SQL ERROR :: " + e);
		}

		st = null;
		return _name;
	}
	
	public static int exe_select2(String sql) {

		int _id = -1;

		PreparedStatement st;

		try {

			st = Jdbc.getConn().prepareStatement(sql);

			ResultSet rs = st.executeQuery();

			if (rs.next()) {
				_id = rs.getInt(1);
			}
			
			// HF.print(String.format("returned id is: %s", _id));
			
			rs = null;

		} catch (SQLException e) {
//			HF.print("SQL ERROR :: " + e);
//			log.System.add_to_log("SQL ERROR :: " + e);
		}
		st = null;
		return _id;
	}
	

	

}
