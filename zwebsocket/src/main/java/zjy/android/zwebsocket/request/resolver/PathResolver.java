package zjy.android.zwebsocket.request.resolver;

import java.util.Arrays;

public class PathResolver implements Resolver {
    @Override
    public void resolve(Chain chain) {
        String temp = chain.residue();
        if (temp.startsWith("/")) {
            int minIndex = Resolver.charMinIndex(temp, new char[]{'?', '&', '#'});
            if (minIndex == -1) {
                String[] paths = temp.replace("/", "").split("/");
                chain.request().setPathSegments(Arrays.asList(paths));
                chain.setResidue("");
            } else {
                String[] paths = temp.substring(0, minIndex).split("/");
                chain.request().setPathSegments(Arrays.asList(paths));
                chain.setResidue(temp.substring(minIndex));
            }
        }
        chain.proceed();
    }
}
