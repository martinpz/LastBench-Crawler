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
 * Debugging messages related to the crawler 
 * 
 * @version: 0.1
 ***/
public class Crawler {
	
	public static void add_to_log(String msg){
		Helper.append_to_file(Config.getCrawlerLogDir(), msg);
	}

}
