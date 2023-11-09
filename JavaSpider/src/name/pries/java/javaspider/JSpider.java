/**
 *
 */
package name.pries.java.javaspider;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;

/**
 *
 */
public class JSpider {

	protected final static Map<String, String> defectiveUris = new TreeMap<>();

	/**
	 * @param args
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void main(final String[] args) throws URISyntaxException, IOException {

		System.out.println("Here I am ...");

		final URI baseUri = new URI("https://de.wikipedia.org");
		// final URI baseUri = new URI("https://www.heise.de/newsticker/");
		// final URI baseUri = new URI("https://www.hgb-leipzig.de");

		final List<URI> uriToDo = new ArrayList<>();
		final Set<URI> uriVisited = new TreeSet<>();
		uriToDo.add(baseUri);

		final String[] hrefs = { "a", "link", "area" };
		final String[] srcs = { "iframe", "img", "script", "source", "track" };

		final Set<URI> srcSet = new TreeSet<>();
		final Set<URI> opaqueUris = new TreeSet<>();
		final List<URL> urlList = new ArrayList<>();

		while (uriToDo.size() > 0) {
			final URI currUri = uriToDo.removeFirst();

			uriVisited.add(currUri);
			urlList.add(currUri.toURL());

			System.err.println("---------------------------------------------");
			System.err.println(currUri.toURL().toString());
			System.err.println("         hrefs: <" + urlList.size() + ">");
			System.err.println("          srcs: <" + srcSet.size() + ">");
			System.err.println("        opaque: <" + opaqueUris.size() + ">");
			System.err.println("defective URIs: <" + defectiveUris.size() + ">");

			final Document doc = getDocument(currUri);
			if (doc == null) {
				defectiveUris.put(currUri.toASCIIString(), "unable to create document");
				continue;
			}

			// get all hrefs
			for (final String href : hrefs) {
				doc.select(href).forEach(e -> {
					final URI newUri = getNewUri(currUri, e.attr("abs:href").trim());
					if ((newUri != null)) {

						if (newUri.isOpaque()) {
							opaqueUris.add(newUri);
							return;
						}

						if (!uriVisited.contains(newUri)) {
							uriToDo.add(newUri);
						}
					}
				});
			}

			// get all srcs
			for (final String src : srcs) {
				doc.select(src).forEach(e -> {
					final URI newUri = getNewUri(currUri, e.attr("abs:src").trim());

					if (newUri.isOpaque()) {
						opaqueUris.add(newUri);
						return;
					}

					if (newUri != null) {
						if (!uriVisited.contains(newUri)) {
							srcSet.add(newUri);
						}
					}
				});
			}

			if (defectiveUris.size() > 200) {
				break;
			}
		}

		final Iterator<String> iter = defectiveUris.keySet().iterator();
		while (iter.hasNext()) {
			final String uri = iter.next();
			System.err.println("<" + uri + "> [" + defectiveUris.get(uri) + "]");
		}

		System.out.println("That's all folks ...!");
	}

	/**
	 * create a new absolute URL
	 *
	 * @param thisUri
	 * @param trim
	 * @return
	 */
	private static URI getNewUri(final URI baseUri, final String src) {

		URI uri = null;

		try {
			uri = new URI(src);

			if (uri.isAbsolute()) {
				return uri.normalize();
			}

			return baseUri.resolve(uri).normalize();

		} catch (final URISyntaxException e) {
			defectiveUris.put(src, "URISyntaxException");
			return null;
		}

	}

	/**
	 * get the document and "handle" all the errors
	 *
	 * @param thisUri
	 * @return
	 */
	private static Document getDocument(final URI thisUri) {
		try {
			return Jsoup.connect(thisUri.toString()).userAgent("Mozilla").timeout(5000).get();
		} catch (final HttpStatusException e) {
			// System.err.println("[HttpStatusException: Unable to get URL: " +
			// thisUri.toString() + "]");
			defectiveUris.put(thisUri.toString(), "HttpStatusException");

		} catch (final IllegalArgumentException e) {
			// System.err.println("[IllegalArgumentException: Unable to get URL: " +
			// thisUri.toString() + "]");
			defectiveUris.put(thisUri.toString(), "IllegalArgumentException");

		} catch (final UnsupportedMimeTypeException e) {
			// System.err.println("[UnsupportedMimeTypeException: Unable to get URL: " +
			// thisUri.toString() + "]");
			defectiveUris.put(thisUri.toString(), "UnsupportedMimeTypeException");

		} catch (final SocketTimeoutException e) {
			// System.err.println("[SocketTimeoutException: Unable to get URL: " +
			// thisUri.toString() + "]");
			defectiveUris.put(thisUri.toString(), "SocketTimeoutException");

		} catch (final MalformedURLException e) {
			// System.err.println("[MalformedURLException: Unable to get URL: " +
			// thisUri.toString() + "]");
			defectiveUris.put(thisUri.toString(), "MalformedURLException");

		} catch (final IOException e) {
			// TODO Auto-generated catch block
			defectiveUris.put(thisUri.toString(), "IOException");
		}

		defectiveUris.put(thisUri.toString(), "unknown reason");

		return null;
	}

}
///
/// EOF
///
/////////////////////////////////////////////////////////////////////////////////////////////
