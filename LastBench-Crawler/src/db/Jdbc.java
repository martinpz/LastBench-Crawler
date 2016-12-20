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

import java.sql.*;
import runner.HF;

/**
 * JDBC driver connetion
 * 
 * @version: 0.1
 ***/
public class Jdbc {

	public static Connection conn = null;

	public Jdbc() {

		log.System.add_to_log("JDBC initialized");

		// Check JDBC Driver available on the current machine

		try {

			Class.forName("com.mysql.jdbc.Driver").newInstance();

//			HF.print("Driver Registration Successful.");
			log.System.add_to_log("jdbc.Driver Registration Successful.");

		} catch (Exception e) {
//			HF.print("!! ERROR @ JDBC" + e);
			log.System.add_to_log("!!ERROR!!" + e);
		}

		// Establishing JDBC connection with MySQL
		try {

			conn = DriverManager.getConnection(Config.getJdbcstring(),
					Config.getUsername(), Config.getPassword());

//			HF.print("Connection to MySQL Database Successful");

			log.System.add_to_log("Connected to MSQL Successfully");
		} catch (Exception e) {
//			HF.print("!! ERROR @ JDBC" + e);
			log.System.add_to_log("!!ERROR!!" + e);
		}
	}

	public static Connection getConn() {
		if (conn == null) {
			new Jdbc();
		}
		return conn;
	}

	public static void closeConn() {
		try {
			conn.close();
		} catch (SQLException e) {
//			HF.print("!! ERROR @ JDBC" + e);
		}
	}

}