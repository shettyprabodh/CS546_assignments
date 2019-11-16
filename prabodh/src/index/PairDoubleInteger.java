package index;

public class PairDoubleInteger implements Comparable<PairDoubleInteger>{
  private Double a;
  private Integer b;

  public PairDoubleInteger(Double a, Integer b){
    this.a = a;
    this.b = b;
  }

  // getters
  public Double getA(){
    return this.a;
  }
  public Integer getB(){
    return this.b;
  }

  @Override
  public int compareTo(PairDoubleInteger other){
    int first_comparison = this.getA().compareTo(other.getA());

    if(first_comparison != 0){
      return first_comparison;
    }
    else{
      return this.getB().compareTo(other.getB());
    }
  }

  @Override
  public String toString(){
    String result = "";

    result += ("Score: " + a);
    result += "\n";
    result += ("Doc id: " + b);
    result += "\n";

    return result;
  }
}
