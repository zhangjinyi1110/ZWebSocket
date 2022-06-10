package zjy.android.zwebsocket.request.resolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QueryParamsResolver implements Resolver {
    @Override
    public void resolve(Chain chain) {
        String temp = chain.residue();
        if (temp.startsWith("?")) {
            temp = temp.replace("?", "&");
        }
        if (temp.startsWith("&")) {
            int end = temp.indexOf("#");
            if (end == -1) {
                List<String> params = new ArrayList<>();
                for (String p : temp.split("&")) {
                    if (p.length() != 0) {
                        if (p.contains("=")) {
                            params.add(p);
                        } else {
                            throw new IllegalArgumentException("query params error, params = " + temp);
                        }
                    }
                }
                chain.request().setQueryParams(params);
                chain.setResidue("");
            } else {
                String pStr = temp.substring(0, end);
                List<String> params = new ArrayList<>();
                for (String p : pStr.split("&")) {
                    if (p.length() != 0) {
                        if (p.contains("=")) {
                            params.add(p);
                        } else {
                            throw new IllegalArgumentException("query params error, params = " + pStr);
                        }
                    }
                }
                chain.request().setQueryParams(params);
                chain.setResidue(temp.substring(end));
            }
        }
        chain.proceed();
    }
}
