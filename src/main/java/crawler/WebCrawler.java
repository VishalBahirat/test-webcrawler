package crawler;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Pattern;

public class WebCrawler {

	private HashSet<String> links;
	private HashSet<String> externalLinks;
	private HashSet<String> staticContent;
	private HashSet<String> errorUrls;

	public WebCrawler() {
		links = new HashSet<String>();
		externalLinks = new HashSet<String>();
		staticContent = new HashSet<String>();
		errorUrls = new HashSet<String>();
	}

	public void getPageLinks(String URL) {
		// 2. Check if you have already crawled the URLs
		//System.out.println("=====WILL BE HITTING ===="+URL);
		if (shouldCrawl(URL)) {
			try {
				// 3. (i) If not add it to the list
				if (links.add(URL)) {
					System.out.println(URL);
				}
			//	System.out.println("####### HITTING ===="+URL);		
				// 4. Fetch the HTML code
				Document document = Jsoup.connect(URL).get();
				// 5. Parse the HTML to extract links to other URLs
				Elements linksOnPage = document.select("a[href]");

				// 6. For each extracted URL... go back to Step 2.
				for (Element page : linksOnPage) {
					getPageLinks(page.attr("abs:href"));
				}
			} catch (IOException e) {
				System.err.println("For '" + URL + "': " + e.getMessage());
				errorUrls.add(URL);
			} catch (IllegalArgumentException e) {
				System.err.println("Cannot hit '" + URL + "': " + e.getMessage());
				errorUrls.add(URL);
			}
		} else {
			// It is an external URL
			externalLinks.add(URL);
		}
	}
	
	public Boolean shouldCrawl(String url) {
		Boolean isValidUrl = isURLInWhiteList(url) && !links.contains(url) && isUrlNotInIgnoreList(url);
		if(isValidUrl && isStaticContent(url)) {
			staticContent.add(url);
			System.out.println("####### STATIC CONTENT ===="+url);
			return false;
		}		
		return isValidUrl;
	}

	private boolean isUrlNotInIgnoreList(String url) {
		// Check for email links, etc & ignore such urls for crawling.
		return !(url.matches("mailto:.*"));
	}

	private boolean isStaticContent(String url) {
		return url.matches(".*css|.*scss|.*jpeg|.*jpg|.*pdf|.*xls|.*xlsx|.*csv");
	}

	private boolean isURLInWhiteList(String url) {
		return StringUtils.contains(url, "prudential.co");
	}

	public static void main(String[] args) {
		// 1. Pick a URL from the frontier
		new WebCrawler().getPageLinks("http://www.prudential.co.uk/");
		
		String url = "mailto:agencyrecruitment_training@prudential.com.gh";
		System.out.println(url.matches("mailto:.*"));
		
		System.out.println(Pattern.compile(".*css^.*pdf").matcher(url).find());
	}

	@Override
	public String toString() {
		return "WebCrawler [CRAWLED LINKS=" + links + ", EXTERNAL LINKS=" + externalLinks + ", STATIC CONTENT=" + staticContent
				+ ", ERROR URLS=" + errorUrls + "]";
	}

}
