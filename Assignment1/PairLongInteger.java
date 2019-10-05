public class PairLongInteger implements Comparable<PairLongInteger>{
  private Long a;
  private Integer b;

  public PairLongInteger(Long a, Integer b){
    this.a = a;
    this.b = b;
  }

  // getters
  public Long getA(){
    return this.a;
  }
  public Integer getB(){
    return this.b;
  }

  @Override
  public int compareTo(PairLongInteger other){
    int first_comparison = this.getA().compareTo(other.getA());

    if(first_comparison != 0){
      return first_comparison;
    }
    else{
      return this.getB().compareTo(other.getB());
    }
  }
}
