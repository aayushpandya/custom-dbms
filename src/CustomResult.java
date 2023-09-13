public class CustomResult<P1, P2>{
    private P1 first;
    private P2 second;
    public CustomResult(P1 first, P2 second){
        this.first = first;
        this.second = second;
    }
    public P1 getFirst(){
        return first;
    }
    public P2 getSecond(){
        return second;
    }
}
