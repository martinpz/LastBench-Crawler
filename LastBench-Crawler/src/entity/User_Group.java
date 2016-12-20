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

package entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import runner.HF;
import db.SQL;

public class User_Group {
	
	int uId;
	
	String relName = null, uName = null;
	
	public User_Group(int uId, String uName, String relName) {
		
		this.uId     = uId;
		this.uName   = uName;
		this.relName = relName;
		
		final String _gUrl = String.format("http://www.last.fm/user/%s/groups", uName);
		
		scrap_page(_gUrl);
		
	}
	
	/**
	 * @param url user_groups_url
	 **/
	public void scrap_page(String url) {
		
		try {
			
			Document doc = Jsoup.connect(url).get();
			
			final String size_html_class = "pagelink";
			
			final String group_html_class = "groupContainer";
			
			final String row_base_url = "%s?groupspage=%s";
			
			final String title = doc.title();
			
			//System.out.println(String.format("Scrapped URL title: %s", title));
			
			final Collection<Element> pages = doc.getElementsByClass(size_html_class);
			final List<String> queued_url = new ArrayList<String>();
			
			queued_url.add(String.format(row_base_url, url, "1"));
			
			// Getting page to be visited later
			
			for (final Element e : pages) {
				final String gNum = e.html();
				final String tmp = String.format(row_base_url, url, gNum);
				
				if (!queued_url.contains(tmp)) {
					queued_url.add(tmp);
					//System.out.println(String.format("%s: ADDED TO QUEUE,", tmp));
				}
			}
			
			// scraping the current page
//			int _groups_size = scrap_single_page(doc, group_html_class);
			int _counter = 0;
			
			String op_cost = "";
			
			long begin_op = System.currentTimeMillis();
			
			for (final String str : queued_url) {
				doc = Jsoup.connect(str).get();
				_counter += scrap_single_page(doc, group_html_class);
			}
			
//			HF.print_sameLine_head(String.format(".. Scrapped GROUP(S): %s", _counter));	
			
			op_cost = HF.get_cost_pure2("Scrapping & Crawler Operations", begin_op);
			
//			HF.print_sameLine_tail(op_cost);
			
			HF.c_log_print("Scrapped GROUP", _counter, Integer.toString(0) ,  op_cost);

			doc = null;
			
		} catch (Exception e) {
			//System.out.println("Catched exception: " + e);
		}
		

	}
	
	//#TODO BUG: here i loop twice on single groups page for a given user
	public int scrap_single_page(Document doc, String group_html_class) {
		
		final Collection<Element> pGroups = doc.getElementsByClass(group_html_class);
		
		if (pGroups.size() > 0){

			for (final Element e : pGroups) {
//				HF.print(String.format("GROUP :: content -> %s", test_grep(e.html())));
				test_grep(e.html());
			}
		}
		
		return pGroups.size();
	}
	
	public String test_grep(String origional) {
		
		origional = origional.replaceAll("\\r|\\n", "");
		
		final String _extract_template = ".*<strong><a.*><img.*>(.*\\w+.*)</a></strong> <p class=\"members\">(\\w+.*) members</p>.*";
		
		final Pattern p = Pattern.compile(_extract_template);
		
		final Matcher m = p.matcher(origional);
		
		if (m.matches()) {
			
			final String _gName = m.group(1); // group name
			final String _gSize = m.group(2); // group # members
			
			long begin; 

			begin = System.currentTimeMillis();

			int _gId = -1;
			
			try{}finally{
				_gId = CGroup.add_new(_gName, _gSize);				
			}
			
			// relationship managed here for user & group
			
			if ( _gId != -1) {
				
				String _row_sql = null, _cmd = null;
				
				_row_sql = "insert into %s (%s_id, %s_id) values (%s, %s)";
				
				_cmd = String.format(_row_sql, relName, "user", "group", uId, _gId);
				
				//HF.print(_cmd);
				
				SQL.exe_insert(_cmd);
				
				return String.format("Name: %s, # Members: %s", m.group(1), m.group(2));
				
			}
			
			HF.get_cost_p(" .. .. ALL_Operations_Group", begin);
			
		}
		
		return "x";
	}
	
}
