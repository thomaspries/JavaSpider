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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;

/**
 *
 */
public class JSpider {

	protected final static List<String> defectiveUris = new ArrayList<>();

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
		final List<URL> urlList = new ArrayList<>();

		while (uriToDo.size() > 0) {
			final URI currUri = uriToDo.removeFirst();

			uriVisited.add(currUri);
			urlList.add(currUri.toURL());

			System.err.println("---------------------------------------------");
			System.err.println(currUri.toURL().toString());
			System.err.println("         hrefs: <" + urlList.size() + ">");
			System.err.println("          srcs: <" + srcSet.size() + ">");
			System.err.println("defective URIs: <" + defectiveUris.size() + ">");

			final Document doc = getDocument(currUri);
			if (doc == null) {
				defectiveUris.add(currUri.toASCIIString());
				continue;
			}

			// get all hrefs
			for (final String href : hrefs) {
				doc.select(href).forEach(e -> {
					final URI newUri = getNewUri(currUri, e.attr("href").trim());
					if ((newUri != null)) {

						if (newUri.isOpaque())
							return;

						if (!uriVisited.contains(newUri)) {
							uriToDo.add(newUri);
						}
					} else {
						defectiveUris.add(e.attr("href").trim());
					}
				});
			}

			// get all srcs
			for (final String src : srcs) {
				doc.select(src).forEach(e -> {
					final URI newUri = getNewUri(currUri, e.attr("src").trim());
					if (newUri != null) {
						if (!uriVisited.contains(newUri)) {
							srcSet.add(newUri);
						}
					} else {
						defectiveUris.add(e.attr("src").trim());
					}
				});
			}

			if (defectiveUris.size() > 1000) {
				break;
			}

		}

		for (final String str : defectiveUris) {
			System.err.println(str);
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
			// handle this URIs later
			if (uri.isOpaque())
				return null;

			if (uri.isAbsolute()) {
				return uri.normalize();
			}
			return baseUri.resolve(uri).normalize();

		} catch (final URISyntaxException e) {
			// TODO Auto-generated catch block
			defectiveUris.add(src);
			// e.printStackTrace();
		}

		return null;
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

		} catch (final IllegalArgumentException e) {
			// System.err.println("[IllegalArgumentException: Unable to get URL: " +
			// thisUri.toString() + "]");

		} catch (final UnsupportedMimeTypeException e) {
			// System.err.println("[UnsupportedMimeTypeException: Unable to get URL: " +
			// thisUri.toString() + "]");

		} catch (final SocketTimeoutException e) {
			// System.err.println("[SocketTimeoutException: Unable to get URL: " +
			// thisUri.toString() + "]");

		} catch (final MalformedURLException e) {
			// System.err.println("[MalformedURLException: Unable to get URL: " +
			// thisUri.toString() + "]");

		} catch (final IOException e) {
			// TODO Auto-generated catch block

		}

		defectiveUris.add(thisUri.toString());

		return null;
	}

	/**
	 * remove fragment vorm URI
	 *
	 * @param uri
	 * @return
	 * @throws URISyntaxException
	 */
	public static URI removeFragment(final URI uri) {
		final String fragment = uri.getFragment();

		if ((fragment != null) && (fragment.length() > 0)) {
			final int index = uri.toASCIIString().lastIndexOf(fragment);
			if (index > -1) {
				final String newStr = uri.toASCIIString().substring(0, index - 1);
				// System.err.println("---------------------------------");
				// System.err.println("URI: <"+uri.toASCIIString()+">");
				// System.err.println("new: <"+newStr+">");
				// System.err.println("---------------------------------");

				if (newStr.length() <= 0)
					return null;
				try {
					return new URI(newStr);
				} catch (final URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}
		}

		return uri;
	}

	/**
	 *
	 * @param base
	 * @param uri
	 * @return
	 */
	public static URI makeAbsolute(final URI base, final URI uri) {

		if (uri == null)
			return base;

		if (uri.isAbsolute())
			return uri;

		if (base != null) {

			try {
				return new URI(base.toASCIIString() + "/" + uri.toASCIIString()).normalize();
			} catch (final URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}

		return null;
	}

}
///
/// EOF
///
/////////////////////////////////////////////////////////////////////////////////////////////
