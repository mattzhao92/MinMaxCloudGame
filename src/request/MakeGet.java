package request;

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
public class MakeGet {

	private URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();

	private Gson g = new Gson();
	private URL url;
  
  
	public MakeGet(String  fullUrl) {
		try {
			this.url = new URL(fullUrl);
		} 
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public String execute() throws Exception {
		HTTPRequest request = new HTTPRequest(url, HTTPMethod.GET);
		return new String(fetcher.fetch(request).getContent());
	}
}
