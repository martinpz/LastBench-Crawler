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

package log;

/**
 * Information related to logger configurations
 * 
 * @version: 0.1
 ***/
class Config {
	
	public static boolean debugerMode = true;
	
	private static final String log_root_dir    = "Logs/%s";
	private static final String crawler_log_dir = "crawler_log.txt";
	private static final String system_log_dir  = "system_log.txt";
		
	public static String getCrawlerLogDir() {
		return String.format(log_root_dir, crawler_log_dir);
	}
	
	public static String getSystemLogDir() {
		return String.format(log_root_dir, system_log_dir);
	}

	
}
