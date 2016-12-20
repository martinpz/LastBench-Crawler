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

package runner;

public class HF {
	
	public static int threshold = 20;
	
	public static void print(String content) {

		String _tmp = "-> " + content;
		
		log.Crawler.add_to_log(_tmp + "\n");
		
		System.out.println(_tmp);
		
	}
	
	public static void print2(String content) {

		String _tmp = ".. " + content;
		
		log.Crawler.add_to_log(_tmp + "\n");
		
		System.out.println(_tmp);
		
	}
	
	
	public static void print_sameLine_head(String content) {

		String _tmp = "-> " + content;
		
		log.Crawler.add_to_log(_tmp);
		
		System.out.print(content);
		
	}
	
	public static void print_sameLine_tail(String content) {

		String _tmp = " (%s)";
		
		_tmp = String.format( _tmp, content );
		
		log.Crawler.add_to_log(_tmp + "\n");
		
		System.out.println(_tmp);
		
	}
	
	/**
	 * Given the started time, calculate the duration till the moment function been called
	 * Return duration in seconds
	 **/
	public static float get_duration(long startTime){
		
		//long end = System.currentTimeMillis();	
		//long dt = end - being;
		//return (int) (dt/1000) % 60;
		
		// compute the elapsed time
		long elapsedSeconds = System.currentTimeMillis()-startTime;
		//display time in seconds
		float dt = elapsedSeconds/1000F;

		return dt;
	}

	public static String get_cost(String title, long begin) {
		
		float result = HF.get_duration(begin);
		
		if ( result > threshold ){
			return String.format(" ____ %s: Finished in %s Sec;", title, HF.get_duration(begin));					
		}
		
		return "";
	}
	
	public static String get_cost_pure(String title, long begin) {
		
		Float result = HF.get_duration(begin);
		
		return String.format(" ____ %s: -- Finished in %s Sec;", title, HF.get_duration(begin));					
		
	}
	
	public static String get_cost_pure2(String title, long begin) {
		
		Float result = HF.get_duration(begin);
		
//		return String.format("%s: %s Sec; ", title, HF.get_duration(begin));					
		return "" + result;
	}
	
	public static void get_cost_p(String title, long begin) {
		
		String _tmp = get_cost(title, begin);
		
		if (_tmp != ""){
			HF.print(_tmp);			
		}
	}
	
	public static void c_log_print (String title, int size, String api_cost, String op_cost){

		api_cost = (api_cost.length() > 5) ? api_cost.substring(0, 5): api_cost;
		op_cost  = (op_cost.length() > 5) ? op_cost.substring(0, 5): op_cost;
		
		String _tmp  = "%15s%5s%5s%60s";
		String _tmp2 = " (%5s%2s%5s%6s ; %20s%2s%5s%6s)";

		String _content = String.format(_tmp2, "API", ":", api_cost, "Sec", "Crawler_Operations", ":", op_cost, "Sec");

		String final_content = String.format(_tmp, title, ":", size, _content);
		
		HF.print2(final_content);
	}

}
