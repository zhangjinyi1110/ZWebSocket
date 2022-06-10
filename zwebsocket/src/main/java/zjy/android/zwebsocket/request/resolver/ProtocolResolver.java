package zjy.android.zwebsocket.request.resolver;

public class ProtocolResolver implements Resolver {
    @Override
    public void resolve(Chain chain) {
        if (chain.url().startsWith("http:")) {
            chain.setResidue(chain.url().substring(7));
        } else if (chain.url().startsWith("https:")) {
            chain.setResidue(chain.url().substring(8));
        } else {
            throw new IllegalArgumentException("utl protocol must is 'http' or 'https', curr url is " + chain.url());
        }
        chain.request().setUrl(chain.url());
        chain.proceed();
    }
}
