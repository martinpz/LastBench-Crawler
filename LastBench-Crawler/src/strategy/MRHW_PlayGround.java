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

package strategy;

import runner.HF;

public class MRHW_PlayGround {

	public static void main(String args[]){
		
		try{
			HF.print("MRHW_PlayGround have been started");
		
		}
		catch(Exception e){
			HF.print("Potential Bug leading to crash; catched at MHRW_Play Ground ..." + e);
		}
		finally{
			MRHW _obj = new MRHW("One-Hop-Walk_16_06","Lauda_L", 2);
			_obj = null;
		}

//		new MRHW("TWO-Hop-Walk","anetrolle", 2);
		
	}
}
