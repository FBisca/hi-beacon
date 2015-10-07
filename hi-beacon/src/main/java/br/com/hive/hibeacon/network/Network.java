package br.com.hive.hibeacon.network;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Single class Network communication
 *
 * Created by FBisca on 07/02/2015.
 */
public class Network {

	/**
	 * Creates a new Network Builder, responsible for network request
	 *
	 * @param url String containing the URL for the new request
	 *
	 * @return Network Builder
	 */
	public static Builder create(String url) {
		return new Builder(url);
	}

	/**
	 * Prepare a HttpURLConnection for subsequent calls
	 *
	 * @param builder Builder with all the params and configurations set
	 *
	 * @return HttpURLConnection created from Network.Builder param
	 *
	 * @throws IOException when connection cannot be made, or timeout is fired
	 */
	private static HttpURLConnection prepareRequest(Builder builder)
			throws IOException {
		if (builder.method.equals(Method.GET)) {
			builder.url = builder.url + preparaGet(builder);
		}

		URL url = new URL(builder.url);
		HttpURLConnection.setFollowRedirects(builder.followRedirects);

		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

		urlConnection.setConnectTimeout(builder.connectionTimeout);
		urlConnection.setReadTimeout(builder.readTimeout);

		urlConnection.setRequestProperty("Accept-Charset", builder.charset);

		if (builder.method.equals(Method.POST)
				|| builder.isRawRequest()) {
			prepareFormEncoded(urlConnection, builder);
		}

		return urlConnection;
	}

	/**
	 * Do the full HTTP request from a Network.Builder params
	 *
	 * @param builder Builder containing all the request configuration
	 *
	 * @return Response object
	 *
	 * @throws IOException when connection cannot be made, or timeout is fired
	 */
	private static Response request(Builder builder)
			throws IOException {

		HttpURLConnection urlConnection = prepareRequest(builder);
		urlConnection.setConnectTimeout(20000);

		InputStream instream = urlConnection.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				instream, builder.charset));
		StringBuilder response = new StringBuilder();
		String line = reader.readLine();

		while (line != null) {
			response.append(line);
			line = reader.readLine();
		}
		instream.close();
		reader.close();

		Response result = new Response();
		result.responseCode = urlConnection.getResponseCode();
		result.response = response.toString();
		result.contentLength = urlConnection.getContentLength();
		result.contentType = urlConnection.getContentType();

		return result;
	}

	/**
	 * Method responsible for setting HTTP Get parameters
	 *
	 * @param builder Builder containing all the parameters
	 *
	 * @return URL String prepared for the HTTP Get
	 *
	 * @throws UnsupportedEncodingException when an URLEnconder error occurs
	 * @throws IllegalArgumentException when a binary parameter is set in the Network.Builder
	 */
	private static String preparaGet(Builder builder) throws UnsupportedEncodingException {
		String query = "";

		if (builder.params != null && builder.params.size() > 0) {
			query += "?";
			Iterator<String> iterator = builder.params.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				Parameter parameter = builder.params.get(key);
				if (parameter.getType().equals(ParameterType.BINARY)) {
					throw new IllegalArgumentException("Parameter of the type \"BINARY\" cannot be set with the HTTP Method \"GET\"");
				}

				String value = key + "=" + URLEncoder.encode(String.valueOf(parameter.getValue()),
						builder.charset);

				if (iterator.hasNext()) {
					value += "&";
				}

				query += value;
			}
		}

		return query;
	}

	/**
	 * Method responsible for filling the urlConnection with the POST Parameters
	 *
	 * @param urlConnection URLConnection to fill.
	 * @param builder Builder containing all POST Parameters
	 *
	 * @throws IOException when an error occurs during writing
	 */
	private static void prepareFormEncoded(URLConnection urlConnection, Builder builder) throws IOException {
		urlConnection.setDoOutput(true);

		if (builder.isRawRequest()) {
			OutputStream output = urlConnection.getOutputStream();
			output.write(builder.mRaw.getBytes(builder.charset));
		} else if (builder.hasMultipart()) {
			prepareMultipart(urlConnection, builder);
		} else {
			String query = "";
			urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + builder.charset);
			Iterator<String> iterator = builder.params.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				Parameter parameter = builder.params.get(key);

				String value = key + "=" + URLEncoder.encode(String.valueOf(parameter.getValue()),
						builder.charset);

				if (iterator.hasNext()) {
					value += "&";
				}

				query += value;
			}

			OutputStream output = urlConnection.getOutputStream();
			output.write(query.getBytes(builder.charset));
		}

	}

	/**
	 * Write a Multipart parameter in the URLConnection
	 *
	 * @param urlConnection URLConnection to be writed
	 * @param builder Builder containing the parameters
	 *
	 * @throws IOException when an error occurs during writing
	 */
	private static void prepareMultipart(URLConnection urlConnection, Builder builder) throws IOException {
		String CRLF = "\r\n";
		String boundary = Long.toHexString(System.currentTimeMillis());

		urlConnection.setUseCaches(false);
		urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
		urlConnection.setRequestProperty("Connection", "Keep-Alive");
		urlConnection.setRequestProperty("Cache-Control", "no-cache");

		OutputStream output = urlConnection.getOutputStream();
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, builder.charset), true);

		for (String key : builder.params.keySet()) {

			Parameter parameter = builder.params.get(key);
			if (!parameter.getType().isMultipart()) {
				writer.append("--").append(boundary).append(CRLF);
				writer.append("Content-Disposition: form-data; name=\"").append(key).append("\"").append(CRLF);
				writer.append("Content-Type: text/plain; charset=").append(builder.charset).append(CRLF);
				writer.append(CRLF).append(parameter.getValue().toString()).append(CRLF).flush();

			} else {
				InputStream stream = (InputStream) parameter.getValue();
				String contentType = URLConnection.guessContentTypeFromStream(stream);

				writer.append("--").append(boundary).append(CRLF);
				writer.append("Content-Disposition: form-data; name=\"").append(key).append("\"; filename=\"").append(key).append("\"").append(CRLF);

				if (contentType != null) {
					writer.append("Content-Type: ").append(contentType).append(CRLF);
				} else {
					writer.append("Content-Type: ").append(parameter.getType().getMimeType()).append(CRLF);
				}

				writer.append("Content-Transfer-Encoding: binary").append(CRLF);
				writer.append(CRLF).flush();
				copy(key, builder, stream, output);
				output.flush();
				writer.append(CRLF).flush();
			}
		}

		writer.append("--").append(boundary).append("--").append(CRLF).flush();
	}

	/**
	 * Write an InputStream into a OutputStream, Network.Listener will be called for the progress.
	 *
	 * @param key Parameter name for listener
	 * @param builder Builder containg the listener for the progress
	 * @param copy Resource
	 * @param to Target
	 *
	 * @throws IOException when an error occurs during writing
	 */
	private static void copy(String key, Builder builder, InputStream copy, OutputStream to) throws IOException {
		int total = copy.available();
		int sent = 0;
		byte[] buf = new byte[1024];
		int len;
		while ((len = copy.read(buf)) > 0) {
			to.write(buf, 0, len);
			if (builder.mListener != null && total > 0) {
				sent += len;
				if (total > 0) {
					float progress = (sent * 100) / total;
					builder.mListener.progress(key, progress, sent, total - sent, total);
				}
			}
		}

	}

	/**
	 * Builder class for a Network request.
	 * This class contains all the information needed for the HTTP request
	 */
	public static class Builder {

		private Map<String, Parameter> params;
		private int connectionTimeout = 15000;
		private int readTimeout = 120000;
		private String url;
		private Method method;
		private String charset;
		private Listener mListener;
		private String mRaw;
		private boolean followRedirects = true;

		/**
		 * Default constructor
		 *
		 * @param url URL for the request
		 *
		 */
		private Builder(String url) {
			this.params = new HashMap<>();
			this.method = Method.GET;
			this.url = url.replaceAll("\\s", "_");
			this.charset = "UTF-8";
		}

		/**
		 * Sets the HTTP method for the current request.<br/>
		 * Use Network.Method
		 *
		 * @param method Method for the HTTP Request
		 *
		 * @return the same Builder instance for consecutive calls
		 */
		public Builder setMethod(Method method) {
			this.method = method;
			return this;
		}

		/**
		 * Sets the Charset for the Request.<br/>
		 * It'll be used for parameters encoding as well
		 *
		 * @param charset Request Charset
		 * @return the same Builder instance for consecutive calls
		 */
		public Builder setCharset(String charset) {
			this.charset = charset;
			return this;
		}

		/**
		 * Sets a listener for the Request.
		 *
		 * @param listener Network.Listener
		 *
		 * @return the same Builder instance for consecutive calls
		 */
		public Builder setListener(Listener listener) {
			this.mListener = listener;
			return this;
		}

		/**
		 * Sets a raw request, that means the request will be exactly as the String set.<br/>
		 * Useful when you need to pass a JSON via POST.
		 *
		 * @param raw Raw Request.
		 * @return the same Builder instance for consecutive calls
		 */
		public Builder setRawRequest(String raw) {
			this.mRaw = raw;
			return this;
		}

		/**
		 * Set <b>true</b> to follow redirects, <b>false</b> otherwise
		 *
		 * @param followRedirects should follow redirects
		 * @return the same Builder instance for consecutive calls
		 */
		public Builder setFollowRedirects(boolean followRedirects) {
			this.followRedirects = followRedirects;
			return this;
		}

		/**
		 * Set a connection timeout in milliseconds.
		 *
		 * @param connectionTimeout connection timeout
		 *
		 * @return the same Builder instance for consecutive calls
		 */
		public Builder setConnectionTimeout(int connectionTimeout) {
			this.connectionTimeout = connectionTimeout;
			return this;
		}

		/**
		 * Set a read timeout in milliseconds.
		 *
		 * @param readTimeout read timeout
		 *
		 * @return the same Builder instance for consecutive calls
		 */
		public Builder setReadTimeout(int readTimeout) {
			this.readTimeout = readTimeout;
			return this;
		}

		/**
		 * Add a parameter for the request.
		 *
		 * @param name Parameter name or key.
		 * @param value Parameter value, toString() it'll be called to define the request value.
		 * @param type Parameter type for describe it upon request
		 *
		 * @return the same Builder instance for consecutive calls
		 */
		public Builder addParameter(String name, Object value, ParameterType type) {
			if (type.isMultipart()) {

				if (value instanceof File) {
					File file = (File) value;

					try {
						value = new FileInputStream(file);
					} catch (FileNotFoundException e) {
						throw new IllegalArgumentException(e.getMessage());
					}

				} else if (!(value instanceof InputStream)) {
					throw new IllegalArgumentException("Parameter of the type \"Multipart\" must be an InputStream or a File");
				}

			}

			params.put(name, new Parameter(name, value, type));
			return this;
		}

		/**
		 * The same as <b>addParameter(String name, Object value, ParameterType type)</b>, but passing ParameterType.Normal as default.
		 *
		 * @param name Parameter name or key.
		 * @param value Parameter value, toString() it'll be called to define the request value.
		 *
		 * @return the same Builder instance for consecutive calls
		 */
		public Builder addParameter(String name, Object value) {
			if (value instanceof File
					|| value instanceof InputStream) {
				return addParameter(name, value, ParameterType.BINARY);
			} else {
				this.addParameter(name, value, ParameterType.NORMAL);
				return this;
			}
		}

		/**
		 * Finish the Builder proccess and make a request with the Builder instance configuration and parameters.
		 *
		 * @return a Network.Response
		 */
		public Network.Response request() {
			try {
				Response response = Network.request(this);
				if (mListener != null) {
					mListener.onComplete(response);
				}
				return response;
			} catch (Exception e) {
				Response response = new Response();
				response.error = e;
				return response;
			}
		}

		/**
		 * Do all the request preparations, but do not fire it.<br/>
		 * Instead return the connection instance.
		 *
		 * @return HttpURLConnection created by the Builder configuration
		 */
		public HttpURLConnection openConnection() {
			try {
				return Network.prepareRequest(this);
			} catch (Exception e) {
				return null;
			}
		}

		/**
		 * The same as the <b>request()</b> but it will run asynchronous.<br/>
		 * Listener need to be set for receive the response.
		 *
		 * It'll run on a AsyncTask.
		 */
		public void requestAsync() {
			new AsyncTask<Builder, Void, Response>() {
				@Override
				protected Response doInBackground(Builder... builders) {
					try {
						return Network.request(builders[0]);
					} catch (Exception e) {
						Response response = new Response();
						response.error = e;
						return response;
					}
				}

				@Override
				protected void onPostExecute(Response s) {
					super.onPostExecute(s);
					if (mListener != null) {
						mListener.onComplete(s);
					}
				}

			}.execute(this);
		}

		/**
		 * If this Builder instance has Binary Parameters
		 * @return <b>true</b> if has Binary Parameters, <b>false</b> otherwise.
		 */
		public boolean hasMultipart() {
			boolean hasMultipart = false;
			for (String key : params.keySet()) {

				Parameter parameter = params.get(key);
				if (parameter.getType().isMultipart()) {
					hasMultipart = true;
				}
			}

			return hasMultipart;
		}

		/**
		 * If this Builder instance has it's a raw request.
		 *
		 * @return <b>true</b> if is a raw request, <b>false</b> otherwise.
		 */
		public boolean isRawRequest() {
			return mRaw != null;
		}
	}

	/**
	 * Class that represents a HTTP Response
	 */
	public static class Response {
		private int responseCode;
		private String response;
		private String contentType;
		private int contentLength;
		private Exception error;

		private Response() {
			super();
		}

		/**
		 * @return the Response content, may be null.
		 */
		@Nullable
		public String getResponse() {
			return response;
		}

		/**
		 * @return the content type, may be null if error is set
		 */
		@Nullable
		public String getContentType() {
			return contentType;
		}

		/**
		 * @return the content length, may be null if error is set
		 */
		@Nullable
		public int getContentLength() {
			return contentLength;
		}

		/**
		 * @return the Network error is has not, may be null if the request was a success
		 */
		@Nullable
		public Exception getError() {
			return error;
		}

		/**
		 * @return the content type, may be null if error is set
		 */
		@Nullable
		public int getResponseCode() {
			return responseCode;
		}
	}

	/**
	 * Class that represents a HTTP Parameter
	 */
	public static class Parameter {

		private String name;
		private Object value;
		private ParameterType type;

		Parameter(String name, Object value, ParameterType type) {
			this.name = name;
			this.value = value;
			this.type = type;
		}

		/**
		 *
		 * @return parameter value
		 */
		Object getValue() {
			return value;
		}

		/**
		 *
		 * @return parameter key/name
		 */
		String getName() {
			return name;
		}

		/**
		 *
		 * @return parameter type, default is NORMAL
		 */
		ParameterType getType() {
			return type;
		}

		@Override
		public String toString() {
			return value == null ? "null" : value.toString();
		}
	}

	public interface Listener {
		/**
		 * Fired when the request is over.<br/>
		 * The Response object always will be set, even if an error has occured.<br/>
		 * Always verify the <b>response.getError()</b> before calling any other response method.
		 *
		 * @param response the response object.
		 */
		void onComplete(Response response);

		/**
		 * Fired upon progress for a Multipart parameter.<br/>
		 * Careful, this method is <b>not</b> fired on a UI Thread.
		 *
		 * @param key parameter key/name
		 * @param progress progress 0f to 100f
		 * @param bytesSent bytes sent to the OutputStream
		 * @param bytesLeft bytes left in the InputStream
		 * @param totalBytes total bytes that InputStream has
		 */
		void progress(String key, float progress, int bytesSent, int bytesLeft, int totalBytes);
	}


	public enum ParameterType {
		/**
		 * Parameter that needs a Multipart, mime-type will try to be defined automatically, but <b>application/octet-stream</b> it's the default value.
		 */
		BINARY(true),
		/**
		 * Text Plain Parameters, that's the default for GET and mostly POST Parameters
		 */
		NORMAL(false),
		/**
		 * Parameter that needs Multipart, mime-type is image/png
		 */
		IMAGE_PNG(true),
		/**
		 * Parameter that needs Multipart, mime-type is image/jpeg
		 */
		IMAGE_JPG(true);

		private boolean multipart = false;

		ParameterType(boolean multipart) {
			this.multipart = multipart;
		}

		/**
		 * @return <b>true</b> if a parameter needs multipart, <b>false</b> otherwise.
		 */
		public boolean isMultipart() {
			return multipart;
		}

		/**
		 * @return mime-type string
		 */
		public String getMimeType() {
			switch (this) {
				case NORMAL:
					return "text/plain";
				case BINARY:
					return "application/octet-stream";
				case IMAGE_PNG:
					return "image/png";
				case IMAGE_JPG:
					return "image/jpeg";
			}
			return "";
		}
	}


	public enum Method {
		GET, POST
	}

}
