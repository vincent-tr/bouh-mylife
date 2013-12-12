package org.mylife.test;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.ConnectionClosedException;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpConnectionFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.ProtocolVersion;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.io.DefaultHttpRequestParserFactory;
import org.apache.http.impl.io.DefaultHttpResponseParserFactory;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicLineParser;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerMapper;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.util.EntityUtils;
import org.mylife.test.TestMain.ProxyHandler.RequestListenerThread;

public class TestMain {

	private final static ProtocolVersion proto = new ProtocolVersion("RTSP", 1,
			0);
	private final static BasicLineParser lineParser = new BasicLineParser(proto);

	public static void main(String[] args) throws IOException {

		DefaultHttpRequestParserFactory requestParserFactory = new DefaultHttpRequestParserFactory(
				lineParser, null);
		DefaultBHttpServerConnectionFactory serverConnectionFactory = new DefaultBHttpServerConnectionFactory(
				null, null, null, requestParserFactory, null);

		final int port = 10554;

		HttpProcessor httpproc = HttpProcessorBuilder.create()
				.add(new ResponseDate()).add(new ResponseServer("Test/1.1"))
				.add(new ResponseContent()).add(new ResponseConnControl())
				.build();

		// Set up request handlers
		// UriHttpRequestHandlerMapper reqistry = new
		// UriHttpRequestHandlerMapper();
		// reqistry.register("*", new ProxyHandler());

		// Set up the HTTP service
		HttpService httpService = new HttpService(httpproc, new Mapper());

		Thread t = new RequestListenerThread(port, httpService,
				serverConnectionFactory);
		t.setDaemon(false);
		t.start();
	}

	static class Mapper implements HttpRequestHandlerMapper {

		@Override
		public HttpRequestHandler lookup(HttpRequest request) {
			return new ProxyHandler();
		}

	}

	static class ProxyHandler implements HttpRequestHandler {

		@Override
		public void handle(HttpRequest request, HttpResponse response,
				HttpContext context) throws HttpException, IOException {

			HttpProcessor httpproc = HttpProcessorBuilder.create()
					.add(new RequestContent()).add(new RequestTargetHost())
					.add(new RequestConnControl())
					.add(new RequestUserAgent("Test/1.1"))
					.add(new RequestExpectContinue(true)).build();

			HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

			HttpCoreContext coreContext = HttpCoreContext.create();
			HttpHost host = new HttpHost("mafreebox.freebox.fr", 554);
			coreContext.setTargetHost(host);

			final int bufferSize = 8 * 1024;
			DefaultHttpResponseParserFactory responseParserFactory = new DefaultHttpResponseParserFactory(
					lineParser, null);
			DefaultBHttpClientConnection conn = new DefaultBHttpClientConnection(
					bufferSize, bufferSize, null, null, null, null, null, null,
					responseParserFactory);
			ConnectionReuseStrategy connStrategy = DefaultConnectionReuseStrategy.INSTANCE;

			Socket socket = new Socket(host.getHostName(), host.getPort());
			conn.bind(socket);

			URI uri = null;
			try {
				uri = new URI(request.getRequestLine().getUri());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}

			String proxyUri = "rtsp://mafreebox.freebox.fr";
			if (uri.getRawPath() != null)
				proxyUri += uri.getRawPath();
			if (uri.getQuery() != null)
				proxyUri += "?" + uri.getQuery();

			BasicHttpRequest proxyRequest = new BasicHttpRequest(request
					.getRequestLine().getMethod(), proxyUri, proto);
			System.out.println(">> Request URI: "
					+ proxyRequest.getRequestLine().getUri());

			for (Header header : request.getAllHeaders())
				proxyRequest.addHeader(header);

			httpexecutor.preProcess(proxyRequest, httpproc, coreContext);
			HttpResponse proxyResponse = httpexecutor.execute(proxyRequest,
					conn, coreContext);
			httpexecutor.postProcess(proxyResponse, httpproc, coreContext);

			System.out.println("<< Response: " + proxyResponse.getStatusLine());
			HttpEntity entity = proxyResponse.getEntity();
			if (entity != null)
				System.out.println(EntityUtils.toString(entity));
			System.out.println("==============");
			if (!connStrategy.keepAlive(response, coreContext)) {
				conn.close();
			}

			for (Header header : proxyResponse.getAllHeaders())
				response.addHeader(header);
			response.setEntity(entity);
		}

		static class RequestListenerThread extends Thread {

			private final HttpConnectionFactory<DefaultBHttpServerConnection> connFactory;
			private final ServerSocket serversocket;
			private final HttpService httpService;

			public RequestListenerThread(final int port,
					final HttpService httpService,
					DefaultBHttpServerConnectionFactory connFactory)
					throws IOException {
				this.connFactory = connFactory;
				this.serversocket = new ServerSocket(port);
				this.httpService = httpService;
			}

			@Override
			public void run() {
				System.out.println("Listening on port "
						+ this.serversocket.getLocalPort());
				while (!Thread.interrupted()) {
					try {
						// Set up HTTP connection
						Socket socket = this.serversocket.accept();
						System.out.println("Incoming connection from "
								+ socket.getInetAddress());
						HttpServerConnection conn = this.connFactory
								.createConnection(socket);

						// Start worker thread
						Thread t = new WorkerThread(this.httpService, conn);
						t.setDaemon(true);
						t.start();
					} catch (InterruptedIOException ex) {
						break;
					} catch (IOException e) {
						System.err
								.println("I/O error initialising connection thread: "
										+ e.getMessage());
						break;
					}
				}
			}
		}

		static class WorkerThread extends Thread {

			private final HttpService httpservice;
			private final HttpServerConnection conn;

			public WorkerThread(final HttpService httpservice,
					final HttpServerConnection conn) {
				super();
				this.httpservice = httpservice;
				this.conn = conn;
			}

			@Override
			public void run() {
				System.out.println("New connection thread");
				HttpContext context = new BasicHttpContext(null);
				try {
					while (!Thread.interrupted() && this.conn.isOpen()) {
						this.httpservice.handleRequest(this.conn, context);
					}
				} catch (ConnectionClosedException ex) {
					System.err.println("Client closed connection");
				} catch (IOException ex) {
					System.err.println("I/O error: " + ex.getMessage());
				} catch (HttpException ex) {
					System.err
							.println("Unrecoverable HTTP protocol violation: "
									+ ex.getMessage());
				} finally {
					try {
						this.conn.shutdown();
					} catch (IOException ignore) {
					}
				}
			}

		}
	}
}
