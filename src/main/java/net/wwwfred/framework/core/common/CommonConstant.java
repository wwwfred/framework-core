package net.wwwfred.framework.core.common;

import java.util.Map;

import net.wwwfred.framework.util.properties.PropertiesUtil;

import com.google.common.collect.Maps;

/**   
 * @Title: CommonConstant.java
 * @Description: 系统通用常量定义
 *
 * @author kevin.jia
 * @date 2015年4月20日 下午4:14:03
 * @version V1.0.0
 */
public class CommonConstant {
    
    private static final String PROPERTIES_CONFIG_FILE_NAME = "config.properties";
    
	public static String CLASS_NAME_PACKAGE_PREFIX = PropertiesUtil.getValue(PROPERTIES_CONFIG_FILE_NAME, "class_name_package_prefix", "com.teshehui");
	
    /**
     * 系统类型定义
     */
    public static enum SYSTEM_TYPE_ENUM {
        
        PRODUCT("01", "商品"), USER("02", "用户"), PAY("03", "支付"), ORDER("04",
                "订单"), SERVER("05", "后台"), STOCK("06", "库存"), IMAGE("07", "图片")
                , PROMOTION("08","促销")
                , ACCOUNT("10", "虚拟账户")
                , CMS("15", "CMS系统"), SEARCH("16", "搜索"), TOP("20", "对接平台")
                , SUPPLIER("25", "供应商"), PORTAL("99", "PORTAL系统");

        private String key;

        private String value;

        private SYSTEM_TYPE_ENUM(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        private static Map<String, String> resultMap = Maps.newHashMap();

        public static Map<String, String> getMap() {
            if(resultMap==null||resultMap.isEmpty())
            {
                for (SYSTEM_TYPE_ENUM element : SYSTEM_TYPE_ENUM.values()) {
                    resultMap.put(element.getKey(), element.getValue());
                }
            }
            return resultMap;
        }
        
        public static SYSTEM_TYPE_ENUM getInstance(String key) {
            for (SYSTEM_TYPE_ENUM one : SYSTEM_TYPE_ENUM.values()) {
                if (one.key.equals(key))
                    return one;
            }
            return null;
        }
    }
    
	/**
	 * 业务类型定义
	 */
	public static enum BUSINESS_TYPE_ENUM {
		
		MALL("01", "商城"), TICKET("02", "机票"), HOTEL("03", "酒店"), FLOWER("04",
				"鲜花"), TUAN("05", "团购")
				, INSURANCE("06", "保险"), MEMBER_CARD("07", "会员卡")
				, O2O("08", "O2O"),CHUANGKE("09","创客")
				,TRAIN("21","高铁"),DIDI("22","滴滴打车")
				,TRAVEL("30","旅游"),CATERING("40","餐饮"),MWQQ("41","美味七七")
				,RECHARGE("50","充值"),TICKET_FILM("60","电影票")
				,DISTRIBUTION("80","分销");

		private String key;

		private String value;

		private BUSINESS_TYPE_ENUM(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}

		private static Map<String, String> resultMap = Maps.newHashMap();

		public static Map<String, String> getMap() {
		    if(resultMap==null||resultMap.isEmpty())
		    {
		        for (BUSINESS_TYPE_ENUM element : BUSINESS_TYPE_ENUM.values()) {
		            resultMap.put(element.getKey(), element.getValue());
		        }
		    }
			return resultMap;
		}
		
		public static BUSINESS_TYPE_ENUM getInstance(String key) {
			for (BUSINESS_TYPE_ENUM one : BUSINESS_TYPE_ENUM.values()) {
				if (one.key.equals(key))
					return one;
			}
			return null;
		}
	}
	
	/**
	 * 终端类型 定义
	 */
	public static enum CLIENT_TYPE_ENUM {
		PC("PC", "PC端"), WAP("WAP", "WAP端"), IPHONE("IPHONE", "IPHONE端"), IPAD(
				"IPAD", "IPAD端"), ANDROID("ANDROID", "ANDROID端");

		private String key;

		private String value;

		private CLIENT_TYPE_ENUM(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}

		private static Map<String, String> resultMap = Maps.newHashMap();

		public static Map<String, String> getMap() {
			if(resultMap==null||resultMap.isEmpty())
			{
			    for (CLIENT_TYPE_ENUM element : CLIENT_TYPE_ENUM.values()) {
			        resultMap.put(element.getKey(), element.getValue());
			    }
			}
			return resultMap;
		}
		
		public static CLIENT_TYPE_ENUM getInstance(String key) {
			for (CLIENT_TYPE_ENUM one : CLIENT_TYPE_ENUM.values()) {
				if (one.key.equals(key))
					return one;
			}
			return null;
		}
	}
	
	/**
	 * 性别类型定义
	 */
    public static enum GENDER_TYPE_ENUM{
        FEMALE("0", "女"), MALE("1", "男"), OTHER("2", "其他");

        private String key;

        private String value;

        private GENDER_TYPE_ENUM(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        private static Map<String, String> resultMap = Maps.newHashMap();

        public Map<String, String> getMap() {
            if(resultMap==null||resultMap.isEmpty())
            {
                for (GENDER_TYPE_ENUM element : GENDER_TYPE_ENUM.values()) {
                    resultMap.put(element.getKey(), element.getValue());
                }
            }
            return resultMap;
        }
        
        public static GENDER_TYPE_ENUM getInstance(String key) {
            for (GENDER_TYPE_ENUM one : GENDER_TYPE_ENUM.values()) {
                if (one.key.equals(key))
                    return one;
            }
            return null;
        }
    }
}
