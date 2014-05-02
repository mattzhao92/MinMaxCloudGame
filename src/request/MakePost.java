/*
 * Copyright (c) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package request;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.gson.Gson;

/**
 * 
 * 
 * @author Joel Baranowski
 */
public class MakePost {

	private URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();

	private Gson g = new Gson();
	private URL url;
  
  
	public MakePost(String  url) {
		try {
			this.url = new URL(url);
		} 
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public String execute(String data) throws Exception {
		HTTPRequest request = new HTTPRequest(url, HTTPMethod.POST);
		request.setPayload(data.getBytes());
		return new String(fetcher.fetch(request).getContent());
	}
}
