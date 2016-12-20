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

public class Config {
	
	// USER limits, sorted desc.

	final static int user_friends      = 500;

	final static int user_topTracks    = 100;
 	final static int user_topArtists   = 20;
	final static int user_groups       = 100;
	
	final static int user_loved_tracks = 100;
	final static int user_recentTracks = 100;
	final static int user_bannedTracks = 100;
	
	final static int user_events       = 50;
	final static int user_neighbours   = 50;
	final static int user_topTags      = 50;
	final static int user_topAlbums    = 20;
	
	// TRACK limits, sorted desc.

	final static int track = 500;

	final static int track_similar = 200; 
	final static int track_topFans = 50;
	final static int track_topTags = 50;
	
	// TRACK artist, sorted desc.
	
	final static int artist_events    = 50;
	final static int artist_similar   = 50;
	final static int artist_topFans   = 50;
	final static int artist_topTags   = 50;
	final static int artist_topTracks = 50;
	
	final static int artist_topAlbums = 20;
	
	// TAG artist, sorted desc.
	
	final static int tag_similar   = 50;
	final static int tag_TopAlbums = 50;
	final static int tag_topArtist = 50;
	final static int tag_TopTracks = 50;
	
	// ALBUM artist, sorted desc.
	
	final static int album_topTags = 50;
	
}
