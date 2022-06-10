package zjy.android.zwebsocket.request.resolver;

import java.util.List;

import zjy.android.zwebsocket.request.Request;

public class ChainImpl implements Resolver.Chain {

    private final Request request;
    private final String url;
    private String residue;
    private int index = 0;
    private final List<Resolver> resolverList;

    public ChainImpl(Request request, String url, List<Resolver> resolverList) {
        this.request = request;
        this.url = url;
        this.resolverList = resolverList;
    }

    @Override
    public Request request() {
        return request;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public String residue() {
        return residue;
    }

    @Override
    public void setResidue(String residue) {
        this.residue = residue;
    }

    @Override
    public void proceed() {
        if (resolverList.size() == index) return;
        resolverList.get(index++).resolve(this);
    }
}
