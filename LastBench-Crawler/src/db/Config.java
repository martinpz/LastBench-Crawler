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

/**
 * Information related to Database configurations
 * 
 * @version: 0.1
 ***/
public class Config {

	 static String dbHost     = "";
	 static String dbName     = "";
 	 static String userName   = "";
	 static String password   = "";
	 
	 static String jdbcString = "jdbc:mysql://%s/%s";
	
	 public static String getJdbcstring() {
		return 	String.format(jdbcString, dbHost, dbName);
	}

	public static String getUsername() {
		return userName;
	}

	public static String getPassword() {
		return password;
	}	     
      
}
