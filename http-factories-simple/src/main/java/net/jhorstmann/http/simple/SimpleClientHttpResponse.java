/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.jhorstmann.http.simple;

import net.jhorstmann.http.shared.AbstractClientHttpResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * {@link ClientHttpResponse} implementation that uses standard JDK facilities.
 * Obtained via {@link SimpleBufferingClientHttpRequest#execute()} and
 * {@link SimpleStreamingClientHttpRequest#execute()}.
 *
 * @author Arjen Poutsma
 * @author Brian Clozel
 * @since 3.0
 */
final class SimpleClientHttpResponse extends AbstractClientHttpResponse {

	private final HttpURLConnection connection;

	private HttpHeaders headers;

	private InputStream responseStream;


	SimpleClientHttpResponse(HttpURLConnection connection) {
		this.connection = connection;
	}


	@Override
	public int getRawStatusCode() throws IOException {
		return this.connection.getResponseCode();
	}

	@Override
	public String getStatusText() throws IOException {
		return this.connection.getResponseMessage();
	}

	@Override
	public HttpHeaders getHeaders() {
		if (this.headers == null) {
			this.headers = new HttpHeaders();
			// Header field 0 is the status line for most HttpURLConnections, but not on GAE
			String name = this.connection.getHeaderFieldKey(0);
			if (StringUtils.hasLength(name)) {
				this.headers.add(name, this.connection.getHeaderField(0));
			}
			int i = 1;
			while (true) {
				name = this.connection.getHeaderFieldKey(i);
				if (!StringUtils.hasLength(name)) {
					break;
				}
				this.headers.add(name, this.connection.getHeaderField(i));
				i++;
			}
		}
		return this.headers;
	}

	@Override
	public InputStream getBody() throws IOException {
		InputStream errorStream = this.connection.getErrorStream();
		this.responseStream = (errorStream != null ? errorStream : this.connection.getInputStream());
		return this.responseStream;
	}

	@Override
	public void close() {
		if (this.responseStream != null) {
			try {
				StreamUtils.drain(this.responseStream);
				this.responseStream.close();
			}
			catch (IOException ex) {
				// ignore
			}
		}
	}

}
