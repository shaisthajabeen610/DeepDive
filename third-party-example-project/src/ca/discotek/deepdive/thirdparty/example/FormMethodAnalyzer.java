package ca.discotek.deepdive.thirdparty.example;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ca.discotek.deepdive.grep.Location;
import ca.discotek.deepdive.security.UnknownFile;
import ca.discotek.deepdive.security.dom.HtmlFile;
import ca.discotek.deepdive.security.dom.JspFile;
import ca.discotek.deepdive.security.misc.HtmlList;
import ca.discotek.deepdive.security.visitor.assess.AbstractDeploymentVisitor;
import ca.discotek.deepdive.security.visitor.assess.AnalyzerException;
import ca.discotek.deepdive.security.visitor.assess.OutputException;

public class FormMethodAnalyzer extends AbstractDeploymentVisitor {

    static final Pattern PATTERN = Pattern.compile("method\\s*=\\s*(\"|')[^'\"]*(\"|')");
    
    StringBuilder buffer = new StringBuilder();

    class Match {
        final String matchingText;
        final int lineNumber;
        
        Match(String matchingText, int lineNumber) {
            this.matchingText = matchingText;
            this.lineNumber = lineNumber;
        }
    }

    class Result {
        final String path;
        final List<Match> matchList = new ArrayList<Match>();
        
        Result(String path) {
            this.path = path;
        }
    }
    
    
    Map<Location, List<Result>> map = new LinkedHashMap<Location, List<Result>>();
    
    public FormMethodAnalyzer(String title, String summary, String description, String links[]) {
        super(title, summary, description, links);
    }

    @Override
    public void visitJsp(JspFile jsp, Location location) throws AnalyzerException {
        processFile(jsp.path, location, jsp.bytes);
    }
    
    @Override
    public void visitHtml(HtmlFile html, Location location) throws AnalyzerException {
        processFile(html.path, location, html.bytes);
    }
    
    @Override
    public void visitUnknownFile(UnknownFile unknownFile, Location location) throws AnalyzerException {
        processFile(unknownFile.getPath(), location, unknownFile.getBytes());
    }
    
    void processFile(String path, Location location, byte bytes[]) {
        Result result = new Result(path);
        try (InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(bytes));
                BufferedReader br = new BufferedReader(isr)) {

            Matcher matcher;
            String line;
            int lineNumber = -1;
            int start;
            while ( (line = br.readLine()) != null) {
                lineNumber++;
                matcher = PATTERN.matcher(line);
                start = 0;
                while (matcher.find(start)) {
                    result.matchList.add(new Match(line.substring(matcher.start(), matcher.end()), lineNumber));
                    start = matcher.end();
                }
            }

            if (result.matchList.size() > 0) {
                List<Result> list = map.get(location);
                if (list == null) {
                    list = new ArrayList<Result>();
                    map.put(location, list);
                }
                list.add(result);
            }
        } 
        catch (IOException e) {
            System.err.println(getClass().getName() + " couldn't process " + path);
            e.printStackTrace();
        }
    }

//    Map<Location, List<Result>> map
    
    @Override
    public void outputToHtml(StringBuilder buffer, boolean includeLinks, Pattern fileFilterPattern) throws OutputException {
        HtmlList topLevelList = new HtmlList(HtmlList.TYPE_UNORDERED, "Form Method References");

        Location location;
        Result result;
        Iterator<Result> resultIterator;
        HtmlList locationList;
        HtmlList matchList;
        Iterator<Match> matchIterator;
        Match match;
        Map.Entry<Location, List<Result>> entries[] = map.entrySet().toArray(new Map.Entry[map.size()]);
        for (int i=0; i<entries.length; i++) {
            location = entries[i].getKey();
            locationList = new HtmlList(HtmlList.TYPE_UNORDERED, location.toString());
            topLevelList.addItem(locationList);
            resultIterator = entries[i].getValue().listIterator();
            while (resultIterator.hasNext()) {
                result = resultIterator.next();
                matchList = new HtmlList(HtmlList.TYPE_UNORDERED, result.path);
                locationList.addItem(matchList);
                matchIterator = result.matchList.listIterator();
                while (matchIterator.hasNext()) {
                    match = matchIterator.next();
                    matchList.addItem(match.matchingText + " : " + match.lineNumber);
                }
            }
        }
        
        buffer.append(topLevelList.toString());
    }
}
