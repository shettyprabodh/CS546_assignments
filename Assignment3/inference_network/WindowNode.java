package inference_network;

import java.util.*;
import index.*;
import java.nio.*;
import java.io.*;

public class WindowNode extends ProximityNode{
  ArrayList<TermNode> children;
  int window_size;
  InvertedIndex index;

  public WindowNode(ArrayList<TermNode> children, int window_size, InvertedIndex index){
    super();
    this.children = children;
    this.window_size = window_size;
    this.index = index;
  }
}
