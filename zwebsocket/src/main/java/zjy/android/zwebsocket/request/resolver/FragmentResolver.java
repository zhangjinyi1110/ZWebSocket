package zjy.android.zwebsocket.request.resolver;

public class FragmentResolver implements Resolver {
    @Override
    public void resolve(Chain chain) {
        String temp = chain.residue();
        if (temp.startsWith("#")) {
            chain.request().setFragment(temp.substring(1));
            chain.setResidue("");
        }
        chain.proceed();
    }
}
