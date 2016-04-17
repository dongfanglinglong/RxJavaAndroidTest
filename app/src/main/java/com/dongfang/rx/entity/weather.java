package com.dongfang.rx.entity;

import java.util.List;

/**
 * Created by dongfang on 2016/4/16.
 *
 */
public class Weather {


    /**
     * city : {"aqi":"42","co":"1","no2":"47","o3":"67","pm10":"28","pm25":"29","qlty":"优","so2":"11"}
     */

    private AqiEntity aqi;
    /**
     * city : 上海
     * cnty : 中国
     * id : CN101020100
     * lat : 31.213000
     * lon : 121.445000
     * update : {"loc":"2016-04-16 14:55","utc":"2016-04-16 06:55"}
     */

    private BasicEntity basic;
    /**
     * cond : {"code":"300","txt":"阵雨"}
     * fl : 21
     * hum : 84
     * pcpn : 0
     * pres : 998
     * tmp : 21
     * vis : 10
     * wind : {"deg":"170","dir":"西风","sc":"3-4","spd":"12"}
     */

    private NowEntity now;
    /**
     * aqi : {"city":{"aqi":"42","co":"1","no2":"47","o3":"67","pm10":"28","pm25":"29","qlty":"优","so2":"11"}}
     * basic : {"city":"上海","cnty":"中国","id":"CN101020100","lat":"31.213000","lon":"121.445000","update":{"loc":"2016-04-16 14:55","utc":"2016-04-16 06:55"}}
     * daily_forecast : [{"astro":{"mr":"13:15","ms":"01:55","sr":"05:24","ss":"18:23"},"cond":{"code_d":"306","code_n":"104","txt_d":"中雨","txt_n":"阴"},"date":"2016-04-16","hum":"82","pcpn":"6.0","pop":"97","pres":"1000","tmp":{"max":"21","min":"15"},"vis":"9","wind":{"deg":"187","dir":"南风","sc":"3-4","spd":"10"}},{"astro":{"mr":"14:09","ms":"02:33","sr":"05:23","ss":"18:24"},"cond":{"code_d":"101","code_n":"305","txt_d":"多云","txt_n":"小雨"},"date":"2016-04-17","hum":"36","pcpn":"0.1","pop":"15","pres":"1017","tmp":{"max":"22","min":"14"},"vis":"10","wind":{"deg":"334","dir":"西北风","sc":"微风","spd":"8"}},{"astro":{"mr":"15:03","ms":"03:10","sr":"05:22","ss":"18:24"},"cond":{"code_d":"101","code_n":"101","txt_d":"多云","txt_n":"多云"},"date":"2016-04-18","hum":"51","pcpn":"0.6","pop":"83","pres":"1019","tmp":{"max":"21","min":"13"},"vis":"10","wind":{"deg":"222","dir":"南风","sc":"微风","spd":"2"}},{"astro":{"mr":"15:55","ms":"03:44","sr":"05:21","ss":"18:25"},"cond":{"code_d":"101","code_n":"104","txt_d":"多云","txt_n":"阴"},"date":"2016-04-19","hum":"51","pcpn":"0.0","pop":"0","pres":"1022","tmp":{"max":"22","min":"14"},"vis":"10","wind":{"deg":"142","dir":"东风","sc":"微风","spd":"7"}},{"astro":{"mr":"16:47","ms":"04:16","sr":"05:20","ss":"18:26"},"cond":{"code_d":"306","code_n":"305","txt_d":"中雨","txt_n":"小雨"},"date":"2016-04-20","hum":"88","pcpn":"9.2","pop":"61","pres":"1013","tmp":{"max":"19","min":"16"},"vis":"2","wind":{"deg":"152","dir":"东南风","sc":"微风","spd":"4"}},{"astro":{"mr":"17:39","ms":"04:49","sr":"05:19","ss":"18:26"},"cond":{"code_d":"305","code_n":"104","txt_d":"小雨","txt_n":"阴"},"date":"2016-04-21","hum":"84","pcpn":"18.6","pop":"58","pres":"1009","tmp":{"max":"21","min":"16"},"vis":"10","wind":{"deg":"335","dir":"东南风","sc":"微风","spd":"3"}},{"astro":{"mr":"18:31","ms":"05:23","sr":"05:17","ss":"18:27"},"cond":{"code_d":"101","code_n":"306","txt_d":"多云","txt_n":"中雨"},"date":"2016-04-22","hum":"86","pcpn":"4.6","pop":"44","pres":"1010","tmp":{"max":"23","min":"14"},"vis":"10","wind":{"deg":"78","dir":"东北风","sc":"微风","spd":"2"}}]
     * hourly_forecast : [{"date":"2016-04-16 16:00","hum":"82","pop":"95","pres":"1000","tmp":"23","wind":{"deg":"232","dir":"西南风","sc":"4-5","spd":"39"}},{"date":"2016-04-16 19:00","hum":"83","pop":"94","pres":"1005","tmp":"22","wind":{"deg":"283","dir":"西北风","sc":"5-6","spd":"46"}},{"date":"2016-04-16 22:00","hum":"84","pop":"36","pres":"1010","tmp":"19","wind":{"deg":"298","dir":"西北风","sc":"4-5","spd":"37"}}]
     * now : {"cond":{"code":"300","txt":"阵雨"},"fl":"21","hum":"84","pcpn":"0","pres":"998","tmp":"21","vis":"10","wind":{"deg":"170","dir":"西风","sc":"3-4","spd":"12"}}
     * status : ok
     * suggestion : {"comf":{"brf":"舒适","txt":"白天不太热也不太冷，风力不大，相信您在这样的天气条件下，应会感到比较清爽和舒适。"},"cw":{"brf":"不宜","txt":"不宜洗车，未来24小时内有雨，如果在此期间洗车，雨水和路上的泥水可能会再次弄脏您的爱车。"},"drsg":{"brf":"较舒适","txt":"建议着薄外套、开衫牛仔衫裤等服装。年老体弱者应适当添加衣物，宜着夹克衫、薄毛衣等。"},"flu":{"brf":"易发","txt":"相对于今天将会出现大幅度降温，空气湿度较大，易发生感冒，请注意适当增加衣服。"},"sport":{"brf":"较不宜","txt":"有较强降水，建议您选择在室内进行健身休闲运动。"},"trav":{"brf":"一般","txt":"温度适宜，但风稍大，且较强降雨的天气将给您的出行带来很多的不便，若坚持旅行建议带上雨具。"},"uv":{"brf":"最弱","txt":"属弱紫外线辐射天气，无需特别防护。若长期在户外，建议涂擦SPF在8-12之间的防晒护肤品。"}}
     */

    private String status;
    /**
     * comf : {"brf":"舒适","txt":"白天不太热也不太冷，风力不大，相信您在这样的天气条件下，应会感到比较清爽和舒适。"}
     * cw : {"brf":"不宜","txt":"不宜洗车，未来24小时内有雨，如果在此期间洗车，雨水和路上的泥水可能会再次弄脏您的爱车。"}
     * drsg : {"brf":"较舒适","txt":"建议着薄外套、开衫牛仔衫裤等服装。年老体弱者应适当添加衣物，宜着夹克衫、薄毛衣等。"}
     * flu : {"brf":"易发","txt":"相对于今天将会出现大幅度降温，空气湿度较大，易发生感冒，请注意适当增加衣服。"}
     * sport : {"brf":"较不宜","txt":"有较强降水，建议您选择在室内进行健身休闲运动。"}
     * trav : {"brf":"一般","txt":"温度适宜，但风稍大，且较强降雨的天气将给您的出行带来很多的不便，若坚持旅行建议带上雨具。"}
     * uv : {"brf":"最弱","txt":"属弱紫外线辐射天气，无需特别防护。若长期在户外，建议涂擦SPF在8-12之间的防晒护肤品。"}
     */

    private SuggestionEntity suggestion;
    /**
     * astro : {"mr":"13:15","ms":"01:55","sr":"05:24","ss":"18:23"}
     * cond : {"code_d":"306","code_n":"104","txt_d":"中雨","txt_n":"阴"}
     * date : 2016-04-16
     * hum : 82
     * pcpn : 6.0
     * pop : 97
     * pres : 1000
     * tmp : {"max":"21","min":"15"}
     * vis : 9
     * wind : {"deg":"187","dir":"南风","sc":"3-4","spd":"10"}
     */

    private List<DailyForecastEntity> daily_forecast;
    /**
     * date : 2016-04-16 16:00
     * hum : 82
     * pop : 95
     * pres : 1000
     * tmp : 23
     * wind : {"deg":"232","dir":"西南风","sc":"4-5","spd":"39"}
     */

    private List<HourlyForecastEntity> hourly_forecast;

    public AqiEntity getAqi() { return aqi;}

    public void setAqi(AqiEntity aqi) { this.aqi = aqi;}

    public BasicEntity getBasic() { return basic;}

    public void setBasic(BasicEntity basic) { this.basic = basic;}

    public NowEntity getNow() { return now;}

    public void setNow(NowEntity now) { this.now = now;}

    public String getStatus() { return status;}

    public void setStatus(String status) { this.status = status;}

    public SuggestionEntity getSuggestion() { return suggestion;}

    public void setSuggestion(SuggestionEntity suggestion) { this.suggestion = suggestion;}

    public List<DailyForecastEntity> getDaily_forecast() { return daily_forecast;}

    public void setDaily_forecast(List<DailyForecastEntity> daily_forecast) { this.daily_forecast = daily_forecast;}

    public List<HourlyForecastEntity> getHourly_forecast() { return hourly_forecast;}

    public void setHourly_forecast(List<HourlyForecastEntity> hourly_forecast) { this.hourly_forecast = hourly_forecast;}

    public static class AqiEntity {
        /**
         * aqi : 42
         * co : 1
         * no2 : 47
         * o3 : 67
         * pm10 : 28
         * pm25 : 29
         * qlty : 优
         * so2 : 11
         */

        private CityEntity city;

        public CityEntity getCity() { return city;}

        public void setCity(CityEntity city) { this.city = city;}

        public static class CityEntity {
            private String aqi;
            private String co;
            private String no2;
            private String o3;
            private String pm10;
            private String pm25;
            private String qlty;
            private String so2;

            public String getAqi() { return aqi;}

            public void setAqi(String aqi) { this.aqi = aqi;}

            public String getCo() { return co;}

            public void setCo(String co) { this.co = co;}

            public String getNo2() { return no2;}

            public void setNo2(String no2) { this.no2 = no2;}

            public String getO3() { return o3;}

            public void setO3(String o3) { this.o3 = o3;}

            public String getPm10() { return pm10;}

            public void setPm10(String pm10) { this.pm10 = pm10;}

            public String getPm25() { return pm25;}

            public void setPm25(String pm25) { this.pm25 = pm25;}

            public String getQlty() { return qlty;}

            public void setQlty(String qlty) { this.qlty = qlty;}

            public String getSo2() { return so2;}

            public void setSo2(String so2) { this.so2 = so2;}
        }
    }

    public static class BasicEntity {
        private String city;
        private String cnty;
        private String id;
        private String lat;
        private String lon;
        /**
         * loc : 2016-04-16 14:55
         * utc : 2016-04-16 06:55
         */

        private UpdateEntity update;

        public String getCity() { return city;}

        public void setCity(String city) { this.city = city;}

        public String getCnty() { return cnty;}

        public void setCnty(String cnty) { this.cnty = cnty;}

        public String getId() { return id;}

        public void setId(String id) { this.id = id;}

        public String getLat() { return lat;}

        public void setLat(String lat) { this.lat = lat;}

        public String getLon() { return lon;}

        public void setLon(String lon) { this.lon = lon;}

        public UpdateEntity getUpdate() { return update;}

        public void setUpdate(UpdateEntity update) { this.update = update;}

        public static class UpdateEntity {
            private String loc;
            private String utc;

            public String getLoc() { return loc;}

            public void setLoc(String loc) { this.loc = loc;}

            public String getUtc() { return utc;}

            public void setUtc(String utc) { this.utc = utc;}
        }
    }

    public static class NowEntity {
        /**
         * code : 300
         * txt : 阵雨
         */

        private CondEntity cond;
        private String fl;
        private String hum;
        private String pcpn;
        private String pres;
        private String tmp;
        private String vis;
        /**
         * deg : 170
         * dir : 西风
         * sc : 3-4
         * spd : 12
         */

        private WindEntity wind;

        public CondEntity getCond() { return cond;}

        public void setCond(CondEntity cond) { this.cond = cond;}

        public String getFl() { return fl;}

        public void setFl(String fl) { this.fl = fl;}

        public String getHum() { return hum;}

        public void setHum(String hum) { this.hum = hum;}

        public String getPcpn() { return pcpn;}

        public void setPcpn(String pcpn) { this.pcpn = pcpn;}

        public String getPres() { return pres;}

        public void setPres(String pres) { this.pres = pres;}

        public String getTmp() { return tmp;}

        public void setTmp(String tmp) { this.tmp = tmp;}

        public String getVis() { return vis;}

        public void setVis(String vis) { this.vis = vis;}

        public WindEntity getWind() { return wind;}

        public void setWind(WindEntity wind) { this.wind = wind;}

        public static class CondEntity {
            private String code;
            private String txt;

            public String getCode() { return code;}

            public void setCode(String code) { this.code = code;}

            public String getTxt() { return txt;}

            public void setTxt(String txt) { this.txt = txt;}
        }

        public static class WindEntity {
            private String deg;
            private String dir;
            private String sc;
            private String spd;

            public String getDeg() { return deg;}

            public void setDeg(String deg) { this.deg = deg;}

            public String getDir() { return dir;}

            public void setDir(String dir) { this.dir = dir;}

            public String getSc() { return sc;}

            public void setSc(String sc) { this.sc = sc;}

            public String getSpd() { return spd;}

            public void setSpd(String spd) { this.spd = spd;}
        }
    }

    public static class SuggestionEntity {
        /**
         * brf : 舒适
         * txt : 白天不太热也不太冷，风力不大，相信您在这样的天气条件下，应会感到比较清爽和舒适。
         */

        private ComfEntity comf;
        /**
         * brf : 不宜
         * txt : 不宜洗车，未来24小时内有雨，如果在此期间洗车，雨水和路上的泥水可能会再次弄脏您的爱车。
         */

        private CwEntity cw;
        /**
         * brf : 较舒适
         * txt : 建议着薄外套、开衫牛仔衫裤等服装。年老体弱者应适当添加衣物，宜着夹克衫、薄毛衣等。
         */

        private DrsgEntity drsg;
        /**
         * brf : 易发
         * txt : 相对于今天将会出现大幅度降温，空气湿度较大，易发生感冒，请注意适当增加衣服。
         */

        private FluEntity flu;
        /**
         * brf : 较不宜
         * txt : 有较强降水，建议您选择在室内进行健身休闲运动。
         */

        private SportEntity sport;
        /**
         * brf : 一般
         * txt : 温度适宜，但风稍大，且较强降雨的天气将给您的出行带来很多的不便，若坚持旅行建议带上雨具。
         */

        private TravEntity trav;
        /**
         * brf : 最弱
         * txt : 属弱紫外线辐射天气，无需特别防护。若长期在户外，建议涂擦SPF在8-12之间的防晒护肤品。
         */

        private UvEntity uv;

        public ComfEntity getComf() { return comf;}

        public void setComf(ComfEntity comf) { this.comf = comf;}

        public CwEntity getCw() { return cw;}

        public void setCw(CwEntity cw) { this.cw = cw;}

        public DrsgEntity getDrsg() { return drsg;}

        public void setDrsg(DrsgEntity drsg) { this.drsg = drsg;}

        public FluEntity getFlu() { return flu;}

        public void setFlu(FluEntity flu) { this.flu = flu;}

        public SportEntity getSport() { return sport;}

        public void setSport(SportEntity sport) { this.sport = sport;}

        public TravEntity getTrav() { return trav;}

        public void setTrav(TravEntity trav) { this.trav = trav;}

        public UvEntity getUv() { return uv;}

        public void setUv(UvEntity uv) { this.uv = uv;}

        public static class ComfEntity {
            private String brf;
            private String txt;

            public String getBrf() { return brf;}

            public void setBrf(String brf) { this.brf = brf;}

            public String getTxt() { return txt;}

            public void setTxt(String txt) { this.txt = txt;}
        }

        public static class CwEntity {
            private String brf;
            private String txt;

            public String getBrf() { return brf;}

            public void setBrf(String brf) { this.brf = brf;}

            public String getTxt() { return txt;}

            public void setTxt(String txt) { this.txt = txt;}
        }

        public static class DrsgEntity {
            private String brf;
            private String txt;

            public String getBrf() { return brf;}

            public void setBrf(String brf) { this.brf = brf;}

            public String getTxt() { return txt;}

            public void setTxt(String txt) { this.txt = txt;}
        }

        public static class FluEntity {
            private String brf;
            private String txt;

            public String getBrf() { return brf;}

            public void setBrf(String brf) { this.brf = brf;}

            public String getTxt() { return txt;}

            public void setTxt(String txt) { this.txt = txt;}
        }

        public static class SportEntity {
            private String brf;
            private String txt;

            public String getBrf() { return brf;}

            public void setBrf(String brf) { this.brf = brf;}

            public String getTxt() { return txt;}

            public void setTxt(String txt) { this.txt = txt;}
        }

        public static class TravEntity {
            private String brf;
            private String txt;

            public String getBrf() { return brf;}

            public void setBrf(String brf) { this.brf = brf;}

            public String getTxt() { return txt;}

            public void setTxt(String txt) { this.txt = txt;}
        }

        public static class UvEntity {
            private String brf;
            private String txt;

            public String getBrf() { return brf;}

            public void setBrf(String brf) { this.brf = brf;}

            public String getTxt() { return txt;}

            public void setTxt(String txt) { this.txt = txt;}
        }
    }

    public static class DailyForecastEntity {
        /**
         * mr : 13:15
         * ms : 01:55
         * sr : 05:24
         * ss : 18:23
         */

        private AstroEntity astro;
        /**
         * code_d : 306
         * code_n : 104
         * txt_d : 中雨
         * txt_n : 阴
         */

        private CondEntity cond;
        private String date;
        private String hum;
        private String pcpn;
        private String pop;
        private String pres;
        /**
         * max : 21
         * min : 15
         */

        private TmpEntity tmp;
        private String vis;
        /**
         * deg : 187
         * dir : 南风
         * sc : 3-4
         * spd : 10
         */

        private WindEntity wind;

        public AstroEntity getAstro() { return astro;}

        public void setAstro(AstroEntity astro) { this.astro = astro;}

        public CondEntity getCond() { return cond;}

        public void setCond(CondEntity cond) { this.cond = cond;}

        public String getDate() { return date;}

        public void setDate(String date) { this.date = date;}

        public String getHum() { return hum;}

        public void setHum(String hum) { this.hum = hum;}

        public String getPcpn() { return pcpn;}

        public void setPcpn(String pcpn) { this.pcpn = pcpn;}

        public String getPop() { return pop;}

        public void setPop(String pop) { this.pop = pop;}

        public String getPres() { return pres;}

        public void setPres(String pres) { this.pres = pres;}

        public TmpEntity getTmp() { return tmp;}

        public void setTmp(TmpEntity tmp) { this.tmp = tmp;}

        public String getVis() { return vis;}

        public void setVis(String vis) { this.vis = vis;}

        public WindEntity getWind() { return wind;}

        public void setWind(WindEntity wind) { this.wind = wind;}

        public static class AstroEntity {
            private String mr;
            private String ms;
            private String sr;
            private String ss;

            public String getMr() { return mr;}

            public void setMr(String mr) { this.mr = mr;}

            public String getMs() { return ms;}

            public void setMs(String ms) { this.ms = ms;}

            public String getSr() { return sr;}

            public void setSr(String sr) { this.sr = sr;}

            public String getSs() { return ss;}

            public void setSs(String ss) { this.ss = ss;}
        }

        public static class CondEntity {
            private String code_d;
            private String code_n;
            private String txt_d;
            private String txt_n;

            public String getCode_d() { return code_d;}

            public void setCode_d(String code_d) { this.code_d = code_d;}

            public String getCode_n() { return code_n;}

            public void setCode_n(String code_n) { this.code_n = code_n;}

            public String getTxt_d() { return txt_d;}

            public void setTxt_d(String txt_d) { this.txt_d = txt_d;}

            public String getTxt_n() { return txt_n;}

            public void setTxt_n(String txt_n) { this.txt_n = txt_n;}
        }

        public static class TmpEntity {
            private String max;
            private String min;

            public String getMax() { return max;}

            public void setMax(String max) { this.max = max;}

            public String getMin() { return min;}

            public void setMin(String min) { this.min = min;}
        }

        public static class WindEntity {
            private String deg;
            private String dir;
            private String sc;
            private String spd;

            public String getDeg() { return deg;}

            public void setDeg(String deg) { this.deg = deg;}

            public String getDir() { return dir;}

            public void setDir(String dir) { this.dir = dir;}

            public String getSc() { return sc;}

            public void setSc(String sc) { this.sc = sc;}

            public String getSpd() { return spd;}

            public void setSpd(String spd) { this.spd = spd;}
        }
    }

    public static class HourlyForecastEntity {
        private String date;
        private String hum;
        private String pop;
        private String pres;
        private String tmp;
        /**
         * deg : 232
         * dir : 西南风
         * sc : 4-5
         * spd : 39
         */

        private WindEntity wind;

        public String getDate() { return date;}

        public void setDate(String date) { this.date = date;}

        public String getHum() { return hum;}

        public void setHum(String hum) { this.hum = hum;}

        public String getPop() { return pop;}

        public void setPop(String pop) { this.pop = pop;}

        public String getPres() { return pres;}

        public void setPres(String pres) { this.pres = pres;}

        public String getTmp() { return tmp;}

        public void setTmp(String tmp) { this.tmp = tmp;}

        public WindEntity getWind() { return wind;}

        public void setWind(WindEntity wind) { this.wind = wind;}

        public static class WindEntity {
            private String deg;
            private String dir;
            private String sc;
            private String spd;

            public String getDeg() { return deg;}

            public void setDeg(String deg) { this.deg = deg;}

            public String getDir() { return dir;}

            public void setDir(String dir) { this.dir = dir;}

            public String getSc() { return sc;}

            public void setSc(String sc) { this.sc = sc;}

            public String getSpd() { return spd;}

            public void setSpd(String spd) { this.spd = spd;}
        }
    }


    @Override
    public String toString() {
        return "Weather{" +
                "aqi=" + aqi +
                ", basic=" + basic +
                ", now=" + now +
                ", status='" + status + '\'' +
                ", suggestion=" + suggestion +
                ", daily_forecast=" + daily_forecast +
                ", hourly_forecast=" + hourly_forecast +
                '}';
    }
}
