package burp;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.commons.net.whois.WhoisClient;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.net.InternetDomainName;

public class DomainObject {
	public String projectName = "";
	public String uploadURL = "Input Upload URL Here";
	public String summary = "";
	public boolean autoAddRelatedToRoot = false; 
	
	private LinkedHashMap<String,String> rootDomainMap = new LinkedHashMap<String,String>();
	// LinkedHashMap to keep the insert order 
	private Set<String> subDomainSet = new HashSet<String>();
	private Set<String> similarDomainSet = new HashSet<String>();
	private Set<String> relatedDomainSet = new HashSet<String>();
	private Set<String> blackDomainSet = new HashSet<String>();

    public static int SUB_DOMAIN=0;
    public static int SIMILAR_DOMAIN=1;
    public static int IP_ADDRESS=2;
    public static int USELESS =-1;
    
    
    DomainObject(){
    	//to resolve "default constructor not found" error
	}
    
    DomainObject(String projectName){
		this.projectName = projectName;
	}
    
    
    public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getUploadURL() {
		return uploadURL;
	}

	public void setUploadURL(String uploadURL) {
		this.uploadURL = uploadURL;
	}

	public boolean isAutoAddRelatedToRoot() {
		return autoAddRelatedToRoot;
	}

	public void setAutoAddRelatedToRoot(boolean autoAddRelatedToRoot) {
		this.autoAddRelatedToRoot = autoAddRelatedToRoot;
	}

	public LinkedHashMap<String, String> getRootDomainMap() {
		return rootDomainMap;
	}

	public void setRootDomainMap(LinkedHashMap<String, String> rootDomainMap) {
		this.rootDomainMap = rootDomainMap;
	}

	public Set<String> getSubDomainSet() {
		return subDomainSet;
	}

	public void setSubDomainSet(Set<String> subDomainSet) {
		this.subDomainSet = subDomainSet;
	}

	public Set<String> getSimilarDomainSet() {
		return similarDomainSet;
	}

	public void setSimilarDomainSet(Set<String> similarDomainSet) {
		this.similarDomainSet = similarDomainSet;
	}

	public Set<String> getRelatedDomainSet() {
		return relatedDomainSet;
	}

	public void setRelatedDomainSet(Set<String> relatedDomainSet) {
		this.relatedDomainSet = relatedDomainSet;
	}



	public Set<String> getBlackDomainSet() {
		return blackDomainSet;
	}

	public void setBlackDomainSet(Set<String> blackDomainSet) {
		this.blackDomainSet = blackDomainSet;
	}


	public String getSummary() {
		summary = String.format("     Project:%s  Related-domain:%s  Sub-domain:%s  Similar-domain:%s  ^_^", projectName, relatedDomainSet.size(),subDomainSet.size(),similarDomainSet.size());
		return summary;
    }
	
	public void setSummary(String Summary) {
		this.summary = Summary;
		
	}
    

	////////////////ser and deser///////////
	
	public String ToJson() {
    	//return JSON.toJSONString(this);
		//https://blog.csdn.net/qq_27093465/article/details/73277291
    	return JSONObject.toJSONString(this);
    }
    
    
    public  static DomainObject FromJson(String instanceString) {// throws Exception {
    	return JSON.parseObject(instanceString, DomainObject.class);
    }
	
    
    // below methods is self-defined, function name start with "fetch" to void fastjson parser error
    

    public String fetchRelatedDomains() {
    	return String.join(System.lineSeparator(), relatedDomainSet);
    }
	
    public String fetchSimilarDomains() {
    	return String.join(System.lineSeparator(), similarDomainSet);
    }
    
    public String fetchSubDomains() {
    	return String.join(System.lineSeparator(), subDomainSet);
    }
    
	public String fetchRootDomains() {
		return String.join(System.lineSeparator(), rootDomainMap.keySet());
	}
	
	
	public Set<String> fetchRootDomainSet() {
		return rootDomainMap.keySet();
	}
	
	public Set<String> fetchKeywordSet(){
		Set<String> result = new HashSet<String>();
		for (String key:rootDomainMap.keySet()) {
			result.add(rootDomainMap.get(key));
		}
		return result;
	}
    
	//没有使用过，主要想考虑oml.jd.local等，非公共域名结尾的情况。暂时先不考虑
	@Deprecated
	public Set<String> fetchSuffixSet(){
		Set<String> result = new HashSet<String>();
		for (String key:rootDomainMap.keySet()) {
			String suffix;
			try {
				//InternetDomainName.from(key).publicSuffix() //当不是com、cn等公共的域名结尾时，将返回空。
				suffix = InternetDomainName.from(key).publicSuffix().toString();
			} catch (Exception e) {
				suffix = key.split(".",2)[1];//分割成2份
			}
			result.add(suffix);
		}
		return result;
	}
    
    
	
    public void AddToRootDomainMap(String key,String value) {
    	if (this.rootDomainMap.containsKey(key) && this.rootDomainMap.containsValue(value)) {
    		//do nothing
    	}else {
    		this.rootDomainMap.put(key,value);
    	}
    }
    
    public void clearWhenRemove() {
    	// TODO
    }
	
	public void relatedToRoot() {
		if (this.autoAddRelatedToRoot == true) {
			for(String relatedDomain:this.relatedDomainSet) {
				if (relatedDomain!=null && relatedDomain.contains(".")) {
		        	String rootDomain =getRootDomain(relatedDomain);
					String keyword = rootDomain.substring(0,rootDomain.indexOf("."));
					if (!rootDomainMap.keySet().contains(rootDomain) && rootDomain != null) {
						rootDomainMap.put(rootDomain,keyword);
					}
				}else {
					System.out.println("error related domain : "+relatedDomain);
				}
			}
			relatedDomainSet.clear();
		}
		//System.out.println(similarDomainSet);
		

        Iterator<String> iterator = similarDomainSet.iterator();
        while(iterator.hasNext()){
        	String similarDomain = iterator.next();
            
            String rootDomain =getRootDomain(similarDomain);
			if (rootDomainMap.keySet().contains(rootDomain) && rootDomain != null) {
				subDomainSet.add(similarDomain);
				iterator.remove();
			}
        }

/*		for (String similarDomain:this.similarDomainSet) {
        	String rootDomain =getRootDomain(similarDomain);
			if (rootDomainMap.keySet().contains(rootDomain) && rootDomain != null) {
				subDomainSet.add(similarDomain);
				similarDomainSet.remove(similarDomain); //lead to "java.util.ConcurrentModificationException" error
			}
		}*/
	}
	
	
    public static String getRootDomain(String inputDomain) {
		try {
			String rootDomain =InternetDomainName.from(inputDomain).topPrivateDomain().toString();
			return rootDomain;
		}catch(Exception e) {
			return null;
			//InternetDomainName.from("www.jd.local").topPrivateDomain()//Not under a public suffix: www.jd.local
		}
	}
    
    public int domainType(String domain) {
		for (String rootdomain:fetchRootDomainSet()) {
			if (rootdomain.contains(".")&&!rootdomain.endsWith(".")&&!rootdomain.startsWith("."))
			{
				if (domain.endsWith("."+rootdomain)||domain.equalsIgnoreCase(rootdomain)){
					return DomainObject.SUB_DOMAIN;
				}
			}
		}
		
		for (String keyword:fetchKeywordSet()) {
			if (!keyword.equals("") && domain.contains(keyword)
					&& InternetDomainName.from(domain).hasPublicSuffix()){//是否是以公开的 .com .cn等结尾的域名。//如果是以比如local结尾的域名，就不会被认可
				return DomainObject.SIMILAR_DOMAIN;
			}
		}
			
		if (Commons.isValidIP(domain)) {//https://202.77.129.30
			return DomainObject.IP_ADDRESS;
		}
		return DomainObject.USELESS;
	}
    
    @Deprecated
    public static String whois(String domainName) {
    	StringBuilder result = new StringBuilder("");

		WhoisClient whois = new WhoisClient();
		try {
			//default is internic.net
			whois.connect(WhoisClient.DEFAULT_HOST);
			String whoisData1 = whois.query("=" + domainName);
			result.append(whoisData1);
			whois.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result.toString();
	}
	
	public static void main(String args[]) {
/*		String Host ="www.baidu.com";
		Set<String> rootdomains = new HashSet<String>();
		rootdomains.add("baidu.com");
		Set<String> keywords = new HashSet<String>();
		keywords.add("baidu");
		
		int type = new DomainObject("").domainType(Host);
		System.out.println(type);*/
		
		DomainObject xx = new DomainObject("");
		xx.getRelatedDomainSet().add("xxx.baidu.com");
		System.out.println(xx.getRelatedDomainSet());
		
		
//		System.out.println(InternetDomainName.from("www.jd.local").publicSuffix());
//		System.out.println(InternetDomainName.from("www.jd.local").topPrivateDomain());
//		System.out.println(whois("jd.ru"));
	}
	
}
