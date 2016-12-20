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

import java.util.Collection;

import entity.Config;
import runner.HF;
import strategy.Api_Catcher;
import strategy.Walk_tracker;
import de.umass.lastfm.Album;
import de.umass.lastfm.Artist;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Event;
import de.umass.lastfm.PaginatedResult;
import de.umass.lastfm.ResponseBuilder;
import de.umass.lastfm.Tag;
import de.umass.lastfm.Track;
import de.umass.lastfm.User;

public class CUser extends CCommon{
	
	final static String tableName  = "user";
	final static String cParams    = "url varchar(100), real_name varchar(255), age int(3), country varchar(100), gender varchar(20), num_playlist int(11), play_count int(11), visited int(1) DEFAULT 0";
	
	public static int id;
	
	/*** CREATING TABLE AND ITS RELATIONSHIPS ***/
	
	public CUser(){
		int_table(tableName, cParams);
		int_relations_tables();
	}

	public void int_relations_tables() {

		db.Lib.cRelTable("user_groups", tableName, "group");
		db.Lib.cRelTable("user_events", tableName, "event");		
		
		db.Lib.cRelTable("user_topArtists", tableName, "artist");
			db.SQL.exe_insert("ALTER TABLE user_topArtists ADD playcount int (15)");

		db.Lib.cRelTable("user_topAlbums", tableName, "album");
			db.SQL.exe_insert("ALTER TABLE user_topAlbums ADD playcount int (15)");
	
		db.Lib.cRelTable("user_topTags", tableName, "tag");
			db.SQL.exe_insert("ALTER TABLE user_topTags ADD count int(15)");

		db.Lib.cRelTable("user_topTracks", tableName, "track");
			db.SQL.exe_insert("ALTER TABLE user_topTracks ADD playcount int (15)");
		
		db.Lib.cRelTable("user_lovedTracks", tableName, "track");
		db.Lib.cRelTable("user_recentTracks", tableName, "track");
		db.Lib.cRelTable("user_bannedTracks", tableName, "track");

		db.Lib.cRelTable("user_friends", tableName, "friend");
		db.Lib.cRelTable("user_neighbours", tableName, "neighbour");
			
	}

	public static void fill_relations(int uId, String uName, int lvl) {
		
		db.Lib.update_to_seen(tableName, uName);
		
		int new_lvl = lvl - 1;
		
		Walk_tracker.increment_crawled_counter();
//		
//		if(Walk_tracker.getCrawled_user() % 10 == 0){
//			HF.print(String.format("%%%%%%%%%%%%%%% CRAWLED USER SO FAR :: ", Walk_tracker.getCrawled_user()));
//		}
			
		HF.print("###");
		HF.print(String.format("{%s} Id: %s, Name: %s; Crawling_lvl: %s", "USER", uId, uName, lvl));
		
		HF.print("##");
		
		try{}finally{
			scrap_groups(uId, uName);
		}
		try{}finally{
			fill_events(uId, uName, "user_events");
		}
		try{}finally{
			fill_top_artists(uId, uName, "user_topArtists");
		}
		try{}finally{
			fill_top_albums(uId, uName, "user_topAlbums");	
		}
		try{}finally{
			fill_top_tags(uId, uName, "user_topTags");
		}
		try{}finally{
			fill_user_relations(uId, uName, new_lvl);
		}
		try{}finally{
			fill_realted_tracks(uId, uName);
		}
		HF.print("##");
	}

	/*** NEW RECORD INTO DB ***/
	
	public static void add_new(String uName, int lvl) {
		
		try {
			User u = User.getInfo(uName, getAPI());
			
			if ( u != null ){
				control_object(u, lvl);
				u = null;
			}
		} catch (Exception e) {
			Api_Catcher.user_error ++;
		}
	}

	public static void add_new(User u, int lvl) {
		
		if ( u != null ){
			control_object(u.getName(), lvl);
			u = null;
		}	
	}
	
	public static int count_friends_api(String uName){
		
		Collection<User> f_list = null;
		
		try{}finally{
			f_list = User.getFriends_2(uName, getAPI());
		}
		
		f_list = null;
		
		return f_list.size();
	}
	
	/*** CHECK OBJECT CONSTRAINTS ***/
	
	public static void control_object(User u, int lvl){
		
		String uName = u.getName();

		control_object(uName, lvl);

		full_info(u);
		
		db.Lib.update_to_completed(tableName, uName);
		
		u = null;
	}
	
	private static void full_info(User u) {
		
		String _tmp = "";
		
		_tmp = "url = '%s', real_name = '%s', age = '%s', country = '%s', gender = '%s', num_playlist = '%s', play_count = '%s', visited = '%s'";
		
		_tmp = String.format(_tmp, db.Lib.clean(u.getUrl()), db.Lib.clean(u.getRealname()), Integer.toString(u.getAge()), u.getCountry(), u.getGender(), Integer.toString(u.getNumPlaylists()), Integer.toString(u.getPlaycount()), Integer.toString(2));

		db.Lib.update(tableName, _tmp, u.getName());
		
		u = null;
		
	}

	public static void control_object(String uName, int lvl){
		
		boolean db_exists = db.Lib.check_exsits(tableName, uName);

		// INSERT NEW RECORD
		if (!db_exists){
				
			//HF.print(String.format("----> USER :: %s; IS NEW TO DB", uName));
			
			id = db.Lib.simpleInsert(tableName, uName);
			
			//HF.print("*** NEWELY INSERTED RECORD IS :: " + id);
		
			if ( lvl > 0 ){
				//#TODO insert other related information, such as: url, country, age, ....
				fill_relations(id, uName, lvl);
			}
		}
		
		// COLLECT INFORMATION FOR OLD RECORD
		if (db_exists){

			boolean isExplored = db.Lib.check_exsits_explored(tableName, uName);
			
			id = db.Lib.get_id(tableName, uName);
			
			if ( lvl > 0 & !isExplored){
				//#TODO insert other related information, such as: url, country, age, ....
				//#TODO: do some magic here
				HF.print(String.format("----> %s :: %s; SEEN IN DB", tableName, uName));
				try{}finally{
					fill_relations(id, uName, lvl);
				}
			}
			
			if ( lvl > 0 & isExplored){
				HF.print(String.format("----> %s :: %s; ALREADY EXPORED IN DB", tableName, uName));
			}
		}
		
	}
	
	/****************** FILL RELATED USER RELATIONSHIP ******************/
	
	/*** Friends & Neighbours ***/
	
	private static void fill_user_relations(int uId, String uName, int lvl){
		
		Collection<User> list_friends = null;
		Collection<User> list_neighbours = null;
		
	
		long begin_api = 0, begin_op = 0;
		String api_cost = "", op_cost = "";
		
		try {
			
			begin_api = System.currentTimeMillis();
			
			list_friends    = User.getFriends(uName, Config.user_friends, getAPI());
			
			//api_cost = HF.get_cost_pure2("API_getFriends()", begin_api);
			api_cost = HF.get_cost_pure2("API", begin_api);
			
			begin_op = System.currentTimeMillis();
			
			user_man_rel_fill(uName, uId, list_friends, "user_friends", "friend", lvl);
			
			//op_cost = HF.get_cost_pure2("ALL_Operations_Friends", begin_op);
			op_cost = HF.get_cost_pure2("Crawler_Operations", begin_op);
			
			if ( list_friends != null){
//				HF.print_sameLine_head(String.format(".. FRIENDS: %s;", list_friends.size()));			
				HF.c_log_print("FRIENDS", list_friends.size(), api_cost, op_cost);
			}
		
//			HF.print_sameLine_tail(api_cost + " " + op_cost);
			
			
			
		} catch (Exception e) {
			Api_Catcher.user_error++;
		}

		begin_api = 0; begin_op = 0;
		api_cost = ""; op_cost = "";
		
		try {
			
			begin_api = System.currentTimeMillis();
			
			list_neighbours = User.getNeighbours(uName, Config.user_neighbours, getAPI());

//			api_cost = HF.get_cost_pure2("API_getNeighbours()", begin_api);
			api_cost = HF.get_cost_pure2("API", begin_api);
			
			begin_op = System.currentTimeMillis();
			
			user_man_rel_fill(uName, uId, list_neighbours, "user_neighbours", "neighbour", lvl);

//			op_cost = HF.get_cost_pure2("ALL_Operations_Neigbours", begin_op);
			op_cost = HF.get_cost_pure2("Crawler_Operations", begin_op);
			
			if ( list_neighbours != null){
//				HF.print_sameLine_head(String.format(".. NEIGHBOURS: %s;", list_neighbours.size()));			
				HF.c_log_print("NEIGHBOURS", list_neighbours.size(), api_cost, op_cost);
			}
			
//			HF.print_sameLine_tail(api_cost + " " + op_cost);
			
			list_neighbours = null; list_friends = null;
			
		} catch (Exception e) {
			Api_Catcher.user_error++;
		}
		
	}

	private static void user_man_rel_fill(String uName, int uId, Collection<User> coll_list, String listName, String obj2CustomName, int lvl) {
				
			if (coll_list.size() > 0) {
				
				for (User fu : coll_list) {
					
					if (fu != null && fu.getName() != null){

						if( !Api_Catcher.firstRun && listName.contains("Neigbours") ){
							try{}finally{
								add_new(fu, 0);
							}
						}
						
						if(Api_Catcher.firstRun){
							Api_Catcher.firstRun = false;
						}
						
						else{
							try{}finally{
							add_new(fu, lvl);
							}
						}
						
						int _id = db.Lib.get_id(tableName, fu.getName());
						
						if (_id != -1){

							String _cmd = String.format("insert into %s (%s_id, %s_id) values (%s, %s)", listName, "user", obj2CustomName, uId, _id);
							
							db.SQL.exe_insert(_cmd);
							
							//HF.print(_cmd);			
							
						}
						
					} else {
						//HF.print(String.format("---...> [IGNORED] element; name: %s", fu.getName()));
					}
					
					fu = null;
				}
			}
		
	}

	/*** RELATED TRACKS ***/
	
	private static void fill_realted_tracks(int uId, String uName){
		
		try {
			
			long begin_api_top = 0, begin_op_top = 0;
			String api_cost_top = "", op_cost_top = "";
			
			long begin_api_banned = 0, begin_op_banned = 0;
			String api_cost_banned = "", op_cost_banned = "";
			
			long begin_api_recent = 0, begin_op_recent = 0;
			String api_cost_recent = "", op_cost_recent = "";
			
			long begin_api_loved = 0, begin_op_loved = 0;
			String api_cost_loved = "", op_cost_loved = "";
			
			//##############

			begin_api_top = System.currentTimeMillis();
			
				Collection<Track> topTracks = User.getTopTracks(uName, Config.user_topTracks, getAPI());
			
//				api_cost_top = HF.get_cost_pure2("API_getTopTracks()", begin_api_top);
				api_cost_top = HF.get_cost_pure2("API", begin_api_top);
				
//			HF.print_sameLine_head(String.format(".. TOP_TRACKS :: %s;", topTracks.size()));
			
			
				begin_op_top = System.currentTimeMillis();
			
				try{}finally{
					gene_tracks("user_topTracks", uId, topTracks); 
				}
				
//				op_cost_top = HF.get_cost_pure2("ALL_Operations_TopTracks", begin_api_top);
				op_cost_top = HF.get_cost_pure2("Crawler_Operations", begin_api_top);
				
//			HF.print_sameLine_tail(api_cost_top + " " + op_cost_top);
			HF.c_log_print("TOP_TRACKS", topTracks.size(), api_cost_top, op_cost_top);
			
			topTracks = null;
		
			//##############
			
			begin_api_banned = System.currentTimeMillis();
			
				Collection<Track> bannedTracks = User.getBannedTracks2(uName, Config.user_bannedTracks, getAPI());
			
//				api_cost_banned = HF.get_cost_pure2("API_getBannedTracks()", begin_api_banned);
				api_cost_banned = HF.get_cost_pure2("API", begin_api_banned);
				
//			HF.print_sameLine_head(String.format(".. BANNED_TRACKS :: %s;", bannedTracks.size()));

				begin_op_banned = System.currentTimeMillis();
			
				gene_tracks("user_bannedTracks", uId, bannedTracks);
			
//				op_cost_banned = HF.get_cost_pure2("ALL_Operations_BannedTracks", begin_op_banned);
				op_cost_banned = HF.get_cost_pure2("Crawler_Operations", begin_op_banned);
				
//				HF.print_sameLine_tail(api_cost_banned + " " + op_cost_banned);
				HF.c_log_print("BANNED_TRACKS", bannedTracks.size(), api_cost_banned, op_cost_banned);
				
				bannedTracks = null;
			
			//##############
			
			begin_api_recent = System.currentTimeMillis();
			
				Collection<Track> recentTracks = User.getRecentTracks(uName, Config.user_recentTracks, getAPI());
			
//				api_cost_recent = HF.get_cost_pure2("API_getRecentTracks()", begin_api_recent);
				api_cost_recent = HF.get_cost_pure2("API", begin_api_recent);
				
//			HF.print_sameLine_head(String.format(".. RECENT_TRACKS :: %s;", recentTracks.size()));
				
				begin_op_recent = System.currentTimeMillis();
			
				gene_tracks("user_recentTracks", uId, recentTracks);
		
//				op_cost_recent = HF.get_cost_pure2("ALL_Operations_RecentTracks", begin_op_banned);
				op_cost_recent = HF.get_cost_pure2("Crawler_Operations", begin_op_banned);
		
//			HF.print_sameLine_tail(api_cost_recent + " " + op_cost_recent);

				HF.c_log_print("RECENT_TRACKS", recentTracks.size(), api_cost_recent, op_cost_recent);
				
				recentTracks = null;
				
			//##############
			
			begin_api_loved = System.currentTimeMillis();
			
				Collection<Track> lovedTracks = User.getLovedTracks2(uName, Config.user_loved_tracks, getAPI());
				
//				api_cost_loved = HF.get_cost_pure2("API_getLovedTracks()", begin_api_loved);
				api_cost_loved = HF.get_cost_pure2("API", begin_api_loved);
			
//			HF.print_sameLine_head(String.format(".. LOVED_TRACKS :: %s;", lovedTracks.size()));
			
				begin_op_recent = System.currentTimeMillis();
			
				gene_tracks("user_lovedTracks", uId, topTracks);
				
//				op_cost_loved = HF.get_cost_pure2("ALL_Operations_LovedTracks", begin_op_banned);
				op_cost_loved = HF.get_cost_pure2("Crawler_Operations", begin_op_banned);
				
//			HF.print_sameLine_tail(api_cost_loved + " " + op_cost_loved);
			
			HF.c_log_print("LOVED_TRACKS", lovedTracks.size(), api_cost_loved, op_cost_loved);
			
			lovedTracks = null;
			
		} catch (Exception e) {
			
			Api_Catcher.user_error++;
		}
	}
	
	private static void gene_tracks(String listName, int uId, Collection<Track> list){
		
		String sql_row = null, _cmd = null;
		
		sql_row = "insert into %s (%s_id, %s_id) values (%s, %s)";
		
		if ( listName.contains("topTracks") ){
			sql_row = "insert into %s (%s_id, %s_id, playcount) values (%s, %s, %s)";
		}
		
		if(list.size() > 0){
			for(Track t : list){

				if (t != null){
					
					if ( listName.contains("topTracks") ){
						try{}finally{
							CTrack.add_new(t, 1);
						}
					}

					try{}finally{	
						CTrack.add_new(t, 0);
					}
					
					int _id = db.Lib.get_id("track", t.getName());
					
					if (_id != -1){
						
						if ( listName.contains("topTracks") ){
							sql_row = "insert into %s (%s_id, %s_id, playcount) values (%s, %s, %s)";
							_cmd = String.format(sql_row, listName, tableName, "track", uId, _id, t.getPlaycount());			
						}
						else {
							_cmd = String.format(sql_row, listName, tableName, "track", uId, _id);						
						}
						
						db.SQL.exe_insert(_cmd);
						
						//HF.print(_cmd);
					}
					
				}
				
				t = null;
			}
		}
		
		list = null;
	
	}
	
	/*** GROUPS ***/
	
	private static void scrap_groups(int uId, String uName) {
		
		try {
			try{}finally{
				User_Group _obj = new User_Group(uId, uName, "user_groups");
				_obj = null;
			}
		} catch (Exception e) {
			Api_Catcher.user_error++;
		}
		
	}
	
	/*** EVENTS ***/
	private static void fill_events(int uId, String uName, String relName) {
	
		Collection<Event> events = null;
		
		String sql_row = null, _cmd = null;
		
		sql_row = "insert into %s (%s_id, %s_id) values (%s, %s)";
		
		//false no festival
		
		long begin_api = 0, begin_op = 0;
		String api_cost = "", op_cost = "";
		
		begin_api = System.currentTimeMillis();
			
		try {
			
			events = User.getEvents(uName, Config.user_events, getAPI()); 

//			api_cost = HF.get_cost_pure2("API_getEvents()", begin_api);
			api_cost = HF.get_cost_pure2("API", begin_api);
			
		} catch (Exception e) {
			Api_Catcher.user_error++;
		}
			
//		HF.print_sameLine_head(String.format(".. EVENTS :: %s;", events.size()));

		begin_op = System.currentTimeMillis();
		
			if(events.size() > 0){
				for(Event e : events){
	
					if (e != null){
						
						int _id = -1;
						
						try{}
						finally{
							_id = CEvent.add_new(e);							
						}

						
						//HF.print(String.format("---> CHECKING; EVENT: name:%s, url: %s, start_date: %s, venue: %ss", e.getTitle(), e.getUrl(), e.getStartDate(), e.getVenue().getName()));
						
						if (_id != -1){
							_cmd = String.format(sql_row, relName, tableName, "event", uId, _id);
							db.SQL.exe_insert(_cmd);
							
							//HF.print(_cmd);
						}
						
					}
					e = null;
				}
			}
		
//			op_cost = HF.get_cost_pure2("ALL_Operations_Events", begin_op);
			op_cost = HF.get_cost_pure2("Crawler_Operations", begin_op);
			
//			HF.print_sameLine_tail(api_cost + " " + op_cost);
			
			HF.c_log_print("EVENTS", events.size(), api_cost, op_cost);
			events = null;
	}
		
	/*** TOP ALBUMS ***/
	
	private static void fill_top_albums(int uId, String uName, String relName){
		
		Collection <Album> albums = null;
		
		String sql_row = null, _cmd = null;
		
		sql_row = "insert into %s (%s_id, %s_id, playcount) values (%s, %s, %s)";
		
		long begin_api = 0, begin_op = 0;
		String api_cost = "", op_cost = "";
		
		begin_api = System.currentTimeMillis();
		
		albums = User.getTopAlbums(uName, Config.user_topAlbums, getAPI());
		
		try {
			albums = User.getTopAlbums(uName, getAPI());
			
//			api_cost = HF.get_cost_pure2("API_Call_getTopAlbums()", begin_api);
			api_cost = HF.get_cost_pure2("API", begin_api);
			
		} catch (Exception e) {
			Api_Catcher.user_error++;
		}
		
//		HF.print_sameLine_head(String.format(".. ALBUMS :: %s;", albums.size()));
		
		begin_op = System.currentTimeMillis();
		
		if(albums.size() > 0){
			
			for(Album a : albums){
				
				if (a != null){
					
					try{}finally{
						CAlbum.add_new(a, 1); // lvl set to 1	
					}
					
					
					int _id = db.Lib.get_id("album", a.getName());
					
					if (_id != -1){
						
						_cmd = String.format(sql_row, relName, tableName, "album", uId, _id, a.getPlaycount());
						
						db.SQL.exe_insert(_cmd);
						
						//HF.print(_cmd);
					}
					
				}
				
				a = null;
			}
		}
	
//		op_cost = HF.get_cost_pure2("ALL_Operations_TopAlbums", begin_op);
		op_cost = HF.get_cost_pure2("Crawler_Operations", begin_op);
		
//		HF.print_sameLine_tail(api_cost + " " + op_cost);
		
//		HF.c_log_print("ALBUMS", albums.size(), api_cost, op_cost);
		
		HF.c_log_print("TOP_ALBUMS", albums.size(), api_cost, op_cost);
		
		albums = null;
	}

	/*** TOP TAGS ***/
	
	private static void fill_top_tags(int uId, String uName, String relName){
		
		Collection <Tag> tags = null;
		
		String sql_row = null, _cmd = null;
		
		sql_row = "insert into %s (%s_id, %s_id, count) values (%s, %s, %s)";
		
		long begin_api = 0, begin_op = 0;
		String api_cost = "", op_cost = "";
		
		begin_api = System.currentTimeMillis();
		
		try {
			
			tags = User.getTopTags(uName, Config.user_topTags, getAPI());
			
//			api_cost = HF.get_cost_pure2("API_Top_Albums()", begin_api);
			api_cost = HF.get_cost_pure2("API", begin_api);

		
		} catch (Exception e) {
			Api_Catcher.user_error++;
		}
	
//		HF.print_sameLine_head(String.format(".. TAGS :: %s;", tags.size()));
		
		begin_op = System.currentTimeMillis();
		
		if(tags.size() > 0){
			
			for(Tag t : tags){
				
				if (t != null){
					
					try{}finally{
						CTag.add_new(t, 1); // lvl set to 1
					}
						
					int _id = db.Lib.get_id("tag", t.getName());
					
					if (_id != -1){
						
						_cmd = String.format(sql_row, relName, tableName, "tag", uId, _id, t.getCount());
						
						db.SQL.exe_insert(_cmd);
						
						//HF.print(_cmd);
					}
					
				}
				t = null;
			}
		}
	
//		op_cost = HF.get_cost_pure2("ALL_Operations_Top_Tags", begin_op);
		op_cost = HF.get_cost_pure2("Crawler_Operations", begin_op);
		
//		HF.print_sameLine_tail(api_cost + " " + op_cost);
		
		HF.c_log_print("TOP_TAGS", tags.size(), api_cost, op_cost);
		
		tags = null;
		
	}

	/****************** TOP ARTISTS ******************/
	
	private static void fill_top_artists(int uId, String uName, String relName){
		
		Collection <Artist> artists = null;
		
		String sql_row = null, _cmd = null;
		
		sql_row = "insert into %s (%s_id, %s_id, playcount) values (%s, %s, %s)";
		
		long begin_api = 0, begin_op = 0;
		String api_cost = "", op_cost = "";
		
		begin_api = System.currentTimeMillis();
		
		try {
			
			artists = User.getTopArtists(uName, Config.user_topArtists, getAPI());

//			api_cost = HF.get_cost_pure2("API_Top_Artists()", begin_api);
			api_cost = HF.get_cost_pure2("API", begin_api);
			
		} catch (Exception e) {
			Api_Catcher.user_error++;
		}
	
//		HF.print_sameLine_head(String.format(".. ARTISTS :: %s;", artists.size()));
		
		begin_op = System.currentTimeMillis();
		
		if(artists.size() > 0){
			
			for(Artist a : artists){
				
				if (a != null){
					
					try{}finally{
						CArtist.add_new(a, 1); // lvl set to 1						
					}
					
					int _id = db.Lib.get_id("artist", a.getName());
					
					if (_id != -1){
						
						_cmd = String.format(sql_row, relName, tableName, "artist", uId, _id, a.getPlaycount());						
						db.SQL.exe_insert(_cmd);
						
//						HF.print(_cmd);
					}
					
				}
				
				a = null;
			}
		}
	
//		op_cost = HF.get_cost_pure2("ALL_Operations_Top_Artist", begin_op);
		op_cost = HF.get_cost_pure2("Crawler_Operations", begin_op);
		
//		HF.print_sameLine_tail(api_cost + " " + op_cost);
		HF.c_log_print("TOP_ARTISTS", artists.size(), api_cost, op_cost);
		
		artists = null;
	}
}
