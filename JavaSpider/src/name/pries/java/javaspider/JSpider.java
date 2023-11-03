/**
 *
 */
package name.pries.java.javaspider;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
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

	/**
	 * @param args
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void main(final String[] args) throws URISyntaxException, IOException {

		System.out.println("Here I am ...");

		final List<URI> uriList = new ArrayList<>();
		final Set<URI> uriArchive = new TreeSet<>();
		final Set<URI> images = new TreeSet<>();

		// final URI baseUri = new URI("https://de.wikipedia.org");
		final URI baseUri = new URI("https://www.hgb-leipzig.de");

		uriList.add(baseUri);

		while (uriList.size() > 0) {
			final URI thisUri = uriList.removeFirst();

			Document doc = null;

			try {
				doc = Jsoup.connect(thisUri.toString()).userAgent("Mozilla").timeout(5000).get();
			} catch (final HttpStatusException e) {
				// System.err.println("[HttpStatusException: Unable to get URL: " +
				// thisUri.toString() + "]");
				continue;
			} catch (final IllegalArgumentException e) {
				// System.err.println("[IllegalArgumentException: Unable to get URL: " +
				// thisUri.toString() + "]");
				continue;
			} catch (final UnsupportedMimeTypeException e) {
				// System.err.println("[UnsupportedMimeTypeException: Unable to get URL: " +
				// thisUri.toString() + "]");
				continue;
			} catch (final SocketTimeoutException e) {
				// System.err.println("[SocketTimeoutException: Unable to get URL: " +
				// thisUri.toString() + "]");
				continue;
			} catch (final MalformedURLException e) {
				// System.err.println("[MalformedURLException: Unable to get URL: " +
				// thisUri.toString() + "]");
				continue;
			}

			// gather all Links
			doc.select("a").forEach(e -> {

				try {
					URI newUri = new URI(e.attr("href").trim());

					final String scheme = (newUri.getScheme() == null) ? (thisUri.getScheme()) : (newUri.getScheme());
					final String host = (newUri.getHost() == null) ? (thisUri.getHost()) : (newUri.getHost());

					// System.err.println("--------------------------------------------------------------");
					// System.err.println("scheme: <"+scheme+">");
					// System.err.println(" host: <"+host+">");
					// System.err.println(" path: <"+newUri.getPath()+">");

					newUri = new URI(scheme, host, newUri.getPath(), null);
					// System.err.println("newUri: <"+newUri.toASCIIString()+">");

					if (host.equals(baseUri.getHost())) {
						if (!uriArchive.contains(newUri)) {
							uriList.add(newUri);
							uriArchive.add(newUri);
							// System.err.println("<URLs: " + uriArchive.size() + ">");
						}
					}

				} catch (final URISyntaxException e1) {
					// System.err.println("<URISyntaxException: " + e.attr("href").trim() + ">");
					return;
				}
			});

			// gather all images from the site
			doc.select("img").forEach(e -> {

				try {

					URI imgUri = new URI(e.attr("src").trim());

					if (imgUri.getHost() == null) {
						// System.err.println("[host==null: " + e.attr("src").trim() + "]");
						imgUri = new URI(thisUri.getScheme(), thisUri.getHost(), imgUri.getPath(), null);
					}
					// System.err.println("[" + newUri.getHost() + "]");

					if (!imgUri.getHost().equals(baseUri.getHost())) {
						System.err.println("===>>> <" + imgUri.getHost() + ">");
					} else {
						System.err.println("[" + thisUri.toASCIIString() + "] -> [" + imgUri.toASCIIString() + "]");
					}

					images.add(imgUri);

				} catch (final URISyntaxException e1) {
					// System.err.println("<URISyntaxException: " + e.attr("href").trim() + ">");
					return;
				}

			});
		}

		System.err.println("URIs: <" + uriArchive.size() + ">");
		System.err.println("IMGs: <" + images.size() + ">");

		// Document doc = Jsoup.connect("https://www.hgb-leipzig.de").get();
		// doc.select("a").forEach(System.out::println);

		System.out.println("That's all folks ...!");
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
