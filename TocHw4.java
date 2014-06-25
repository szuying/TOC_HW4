/*
 * author: 陳思穎
 * student ID: H34001165
 * date: 2014/06/25
 * 
 * Given a URL,
 * scan the whole data(.json) and find out
 * "which road in a city has house trading records
 * spread in #max_distinct_month."
 * Print out the roads name and their cities
 * with their highest sale price and lowest sale price.
 */
 
import org.json.*;
import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class TocHw4 {
	public static void main(String[] args) throws Exception {

		//String url = "http://www.datagarage.io/api/5385b69de7259bb37d925971";
		String url=args[0];
		String road_patt = ".*路";
		String street_patt = ".*街";
		String ave_patt = ".*大道";
		String lane_patt = ".*巷";
		Pattern road_pattern = Pattern.compile(road_patt);
		Pattern street_pattern = Pattern.compile(street_patt);
		Pattern ave_pattern = Pattern.compile(ave_patt);
		Pattern lane_pattern = Pattern.compile(lane_patt);
		
		// get json file
		URL data_url = new URL(url);
		URLConnection url_con = data_url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				url_con.getInputStream(), "UTF-8"));
		String inputLine;
		int line = 0;

		class Data implements Comparable<Object> {
			String addr = "";
			int count = 0;
			int low, high;
			Map<Integer, Integer> dateMap = new HashMap<Integer, Integer>();
			public int compareTo(Object anotherData) throws ClassCastException {
				if (!(anotherData instanceof Data))
					throw new ClassCastException("A Data object expected.");
				int anotherDataCount = ((Data) anotherData).count;
				return anotherDataCount - this.count;
			}
		}
		Data[] dataArr = new Data[2005];
		for (int i = 0; i < 2005; ++i) {
			dataArr[i] = new Data();
		}
		
		// map address to an index
		Map<String, Integer> addrMap = new HashMap<String, Integer>();
		int addrIndex = 0;

		while ((inputLine = in.readLine()) != null) {
			if (line != 0 && inputLine.charAt(0) == '{') {
				JSONObject jsonObj = new JSONObject(inputLine);
				String road = jsonObj.getString("土地區段位置或建物區門牌");
				int date = jsonObj.getInt("交易年月");
				int price = jsonObj.getInt("總價元");

				Matcher road_matcher = road_pattern.matcher(road);
				Matcher street_matcher = street_pattern.matcher(road);
				Matcher ave_matcher = ave_pattern.matcher(road);
				Matcher lane_matcher = lane_pattern.matcher(road);
				int found = 0;
				int road_len = 0, street_len = 0, ave_len = 0;
				String roadname = "", streetname = "", avename = "", lanename = "", addrname = "";
				
				if (road_matcher.find()) {
					roadname = road_matcher.group();
					road_len = roadname.length();
					addrname = roadname;
					++found;
				}
				if (street_matcher.find()) {
					streetname = street_matcher.group();
					street_len = streetname.length();
					addrname = streetname;
					++found;
				}
				if (ave_matcher.find()) {
					avename = ave_matcher.group();
					ave_len = avename.length();
					addrname = avename;
					++found;
				}
				if (lane_matcher.find() && found == 0) {
					lanename = lane_matcher.group();
					found = 10;
				}
				if (found == 10)
					addrname = lanename;
				else if (found > 1) {
					
					// find the longest name (problem 大道路)
					int max_len = 0;
					if (road_len > max_len) {
						addrname = roadname;
						max_len = road_len;
					}
					if (street_len > max_len) {
						addrname = streetname;
						max_len = street_len;
					}
					if (ave_len > max_len) {
						addrname = avename;
						max_len = ave_len;
					}
				}
				if (found != 0) {
					//System.out.println(addrname + " " + date + " " + price);
					
					// the address has been found
					if (addrMap.get(addrname) != null) {
						int index = addrMap.get(addrname);
						if (price > dataArr[index].high)
							dataArr[index].high = price;
						if (price < dataArr[index].low)
							dataArr[index].low = price;
						if (dataArr[index].dateMap.get(date) == null) {
							++dataArr[index].count;
							dataArr[index].dateMap.put(date,
									dataArr[index].count);
						}

					}
					// the address is found the 1st time
					else {
						addrMap.put(addrname, addrIndex);
						dataArr[addrIndex].addr = addrname;
						dataArr[addrIndex].count = 1;
						dataArr[addrIndex].high = price;
						dataArr[addrIndex].low = price;
						dataArr[addrIndex].dateMap.put(date,
								dataArr[addrIndex].count);
						++addrIndex;
					}
				}
			}
			line++;
		}
		
		// sort the arr by count
		Arrays.sort(dataArr);
		int max_count = dataArr[0].count;
		
		// if more than one data have the same num of count
		for (int i = 0;; ++i) {
			if (dataArr[i].count != max_count)
				break;
			System.out.println(dataArr[i].addr + ", 最高成交價: " + dataArr[i].high
					+ ", 最低成交價: " + dataArr[i].low);
		}
		in.close();
	}
}
