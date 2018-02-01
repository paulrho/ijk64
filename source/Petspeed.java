////////////////////////////////////////////////
//
// Petspeed 'er
// Goes much (26x) faster
//
////////////////////////////////////////////////
class Petspeed
{
  static final int MAX=5000;
  // store program memory
  int top=1; // because 0 is a special pointer to show invalid
  int tmptop;
  boolean record=false;
  int []prog= new int[MAX]; // program memory instruction
  double [] pargD = new double[MAX]; // double arg
  int [] pargmem = new int[MAX]; // mem pointer arg
  String [] pargS = new String[MAX]; // string arg
  Machine using_machine=null;

  static final int MAXPSIZE=50000; // just for now, have a more efficient sparse array later
  int []acpointer = new int[MAXPSIZE];
  int []acpointer_next = new int[MAXPSIZE];

  // to fix -> make this take less space! we are now taking 250k!
  int []pnext = new int[MAXPSIZE];
  int []pcache = new int[MAXPSIZE];
  int []btpnt = new int[MAXPSIZE]; // only used for basictimer

  Petspeed(Machine m) {
    using_machine=m;
  }
  // storing the program
  int getTop() { return top; }
  int addInstr(int instr) { return addInstr(instr, 0.0,-1, ""); }
  int addInstr(int instr, String argS) { return addInstr(instr, 0.0,-1, argS); }
  int addInstr(int instr, double argD, int argmem) { return addInstr(instr, argD, argmem, ""); }
  int addInstr(int instr, double argD) { return addInstr(instr, argD, -1, ""); }
  int addInstr(int instr, int argmem)  { return addInstr(instr, 0.0, argmem, ""); }
  int addInstr(int instr, double argD, int argmem, String argS) {
    if (record) {
      prog[tmptop]=instr;
      pargD[tmptop]=argD;
      pargmem[tmptop]=argmem;
      pargS[tmptop]=argS;
      if (using_machine.verbose) System.out.printf("%d %d %d %f %s\n",tmptop,prog[tmptop],pargmem[tmptop],pargD[tmptop],pargS[tmptop]);
      //if (instr<0) System.out.printf("-1 INSTRUCTION\n"); // debug FIX
      tmptop++;
      return 1;
    } else return 0;
  }
  void rewind() {
    tmptop--;
  }
   
  static int ftoken(String f) {
    for (int i=0; i<O_strings.length; ++i)
      if (O_strings[i].equals(f)) return i;
    for (int i=0; i<F_strings.length; ++i)
      if (F_strings[i].equals(f)) return i;
    //if (using_machine.verbose) System.out.printf("A-COMPILER could not find %s (okay if array)\n",f);
    return -1;
  }

  //////////////////////////////////////////////////////////////////////
  /// the a-machine
  //////////////////////////////////////////////////////////////////////
  static final int AMAX=100;
  double astack_d[]=new double[AMAX];
  String astack_s[]=new String[AMAX];
  int atop=0;

  int listtop=0;
  GenericType list;
  void listadd(double d) {
    if (listtop==0) list=new GenericType(d);
    else list.add(d,20); // hard code 20 for now FIX
    listtop++;
  }
  void listadd(String s) {
    if (listtop==0) list=new GenericType(s);
    else list.add(s,20); // hard code 20 for now FIX
    listtop++;
  }

  java.text.SimpleDateFormat localDateFormat = new java.text.SimpleDateFormat("HHmmss"); // for TI$ efficiency

  int execute(int x) throws EvaluateException {
    listtop=atop; // extra overhead - want to avoid this if can - FIX
    boolean verbose=using_machine.verbose;

    try { // safety catch!
    for (int i=acpointer[x]; i<MAX; ++i) {
      //if (prog[i]==I_HLT) break;
      if (verbose) System.out.printf("EXECUTING %d %d %d %f %s  ",i,prog[i],pargmem[i],pargD[i],pargS[i]);
      switch(prog[i]) {
        case I_HLT: 
          if (verbose) System.out.printf("\n");
	  if (listtop==0 && atop!=0 && atop!=1) System.out.printf("atop not at zero or one (%d)\n",atop);
	  if (verbose && listtop>0) { System.out.printf("pushed %d onto gt list\n",listtop); }
	  return nextpnt(x);
	case I_PRF | T_Str :
          if (verbose) System.out.printf("Return parameter %d flagged as a string stack=%d ",listtop,atop);
          listadd(astack_s[listtop]);
	  break;
	case I_PRF | T_Dbl :
          if (verbose) System.out.printf("Return parameter %d flagged as a double stack=%d ",listtop,atop);
          listadd(astack_d[listtop]);
	  break;
	case I_FNC | F_sin : 
	  astack_d[atop-1]= Math.sin(astack_d[atop-1]);
	  break;
	case I_FNC | F_cos : 
	  astack_d[atop-1]= Math.cos(astack_d[atop-1]);
	  break;
	case I_FNC | F_int : 
	  astack_d[atop-1]= (double)((int)(astack_d[atop-1]));
	  break;
	case I_FNC | F_log : 
	  astack_d[atop-1]= Math.log(astack_d[atop-1]);
	  break;
	case I_FNC | F_tan : 
	  astack_d[atop-1]= Math.tan(astack_d[atop-1]);
	  break;
	case I_FNC | F_atn : 
	  astack_d[atop-1]= Math.atan(astack_d[atop-1]);
	  break;
	case I_FNC | F_sqr : 
	case I_FNC | F_sqrt : 
	  astack_d[atop-1]= Math.sqrt(astack_d[atop-1]);
	  break;
	case I_FNC | F_asin : 
	  astack_d[atop-1]= Math.asin(astack_d[atop-1]);
	  break;
	case I_FNC | F_acos : 
	  astack_d[atop-1]= Math.acos(astack_d[atop-1]);
	  break;
	case I_FNC | F_abs : 
	  astack_d[atop-1]= Math.abs(astack_d[atop-1]);
	  break;
	case I_FNC | F_rnd : 
          if (astack_d[atop-1]<0) { using_machine.evaluate_engine.generator = new java.util.Random((long)-astack_d[atop-1]); } // added this in for seeds
	  astack_d[atop-1]=using_machine.evaluate_engine.generator.nextDouble();
	  break;
	case I_FNC | F_exp : 
	  astack_d[atop-1]= Math.exp(astack_d[atop-1]);
	  break;
	case I_FNC | F_sgn : 
	  if (astack_d[atop-1]<0.0) astack_d[atop-1]=-1.0;
	  else if (astack_d[atop-1]==0.0) astack_d[atop-1]=0.0;
	  else astack_d[atop-1]=1.0;
	  break;

	case I_FNC | F_val : 
          // takes a string but returns a double
          try {
            astack_d[atop-1]=Double.parseDouble(astack_s[atop-1]); 
          } catch (Exception e) { 
            // maybe just a "." should be coverted to 0 or a ""???
            // throw new EvaluateException("NON NUMERIC STRING"); // look at what the C64 really does
            astack_d[atop-1]=0.0;
          }
	  break;

	case I_FNC | F_instr : 
          astack_d[atop-2]=0;
          for (int j=0; j<=astack_s[atop-2].length()-astack_s[atop-1].length(); ++j) {
            if (astack_s[atop-2].substring(j,j+astack_s[atop-1].length()).equals(astack_s[atop-1])) {
              astack_d[atop-2]=j+1;
              break;
            }
          }
	  atop--;
	  break;

	case I_FNC | F_frm : 
          try {          
            astack_s[atop-2]=String.format(astack_s[atop-2],astack_d[atop-1]);
	    atop--;
          } catch(Exception e) {
            throw new EvaluateException("BAD FORMAT");              
          }
	  break;
	case I_FNC | F_midD : 
          try {          
	    if (pargmem[i]==3) {
	      astack_s[atop-3]=astack_s[atop-3].substring((int)astack_d[atop-2]-1,(int)astack_d[atop-2]-1+(int)astack_d[atop-1]);
	      atop--;
	      atop--;
	    } else {
	      astack_s[atop-2]=astack_s[atop-2].substring((int)astack_d[atop-1]-1);
	      atop--;
	    }
          } catch(Exception e) {
            throw new EvaluateException("BAD SUBSTRING INDEX");              
          }
	  break;
	case I_FNC | F_leftD : 
          //try {          
	    //astack_s[atop-2]=astack_s[atop-2].substring(0,(int)astack_d[atop-1]);

            if ((int)astack_d[atop-1]<=0) { astack_s[atop-2]=""; }
            else if ((int)astack_d[atop-1]>astack_s[atop-2].length()) { }
            else { astack_s[atop-2]=astack_s[atop-2].substring(0,(int)astack_d[atop-1]); }
	    atop--;
          //} catch(Exception e) {
            //throw new EvaluateException("BAD SUBSTRING INDEX");              
          //}
	  break;
	case I_FNC | F_rightD : 
          try {          
	    astack_s[atop-2]=astack_s[atop-2].substring(astack_s[atop-2].length()-(int)astack_d[atop-1]);
	    atop--;
          } catch(Exception e) {
            throw new EvaluateException("BAD SUBSTRING INDEX");              
          }
	  break;
	case I_FNC | F_strD : 
	  // I think the leading space is wrong - it should be -ve if it is.... FIX
	  if (astack_d[atop-1]-(int)astack_d[atop-1]==0.0) {
	    astack_s[atop-1]=" "+(int)astack_d[atop-1];
          } else {
	    astack_s[atop-1]=" "+(new Double(astack_d[atop-1]).toString());
          }
	  break;
	case I_FNC | F_len : 
          try {          
	    astack_d[atop-1]=astack_s[atop-1].length();
          } catch(Exception e) {
            throw new EvaluateException("BAD SUBSTRING INDEX");              
          }
	  break;

	case I_FNC | F_peek : 
	  astack_d[atop-1]=using_machine.peek((int)astack_d[atop-1]);
	  break;
	case I_FNC | F_pos : 
	  {
            int cursx=using_machine.machinescreen.cursX;
            astack_d[atop-1]=cursx;
          }
	  break;

	case I_FNC | F_asc : 
	  {
            if (astack_s[atop-1]=="") throw new EvaluateException("ILLEGAL QUANTITY");
            int v=(int)((astack_s[atop-1].charAt(0))&0xFF);
            if (v>=65&&v<=90) v+=128; 
            else if (v>=65&&v<=90) v+=32;
            else if (v>=97&&v<=122) v-=32; 
            else if (v>=193&&v<=218) v-=96; 
            else if (v==95) v=96; 
            else if (v==96) v=95; 
            else if (v==123) v=179; else if (v==179) v=123;
            else if (v==124) v=125; 
            else if (v==125) v=171; 
            else if (v==171) v=124;  
            astack_d[atop-1]=v;
	  }
	  break;
	case I_FNC | F_chrD : 
	  {
            int v=(int)astack_d[atop-1];
            if (v>=193&&v<=218) v=v-128; 
            else if (v>=65&&v<=90) v=v+32; 
            else if (v>=97&&v<=122) v=v+96; 
            else if (v==95) v=96; 
            else if (v==96) v=95; 
            else if (v==123) v=179; else if (v==179) v=123;
            else if (v==124) v=171; 
            else if (v==125) v=124; 
            else if (v==171) v=125;  
            astack_s[atop-1]=""+(char)v;
	  }
	  break;

	case I_FNC | F_var_ti : 
	  astack_d[atop++]= (double)(int)((System.currentTimeMillis()/16.66666666)%1073741824);
	  break;
	case I_FNC | F_var_st : 
	  astack_d[atop++]= 0; // FIX
	  break;
	case I_FNC | F_var_tiD : 
	  astack_s[atop++]= localDateFormat.format( System.currentTimeMillis());
	  break;
	case I_FNC | F_var_pi : 
	  astack_d[atop++]= Math.PI;
	  break;

	case I_PRF | T_Str | O_add : 
	  astack_s[atop-2]= astack_s[atop-2] + astack_s[atop-1]; atop--;
	  break;

	case I_PRF | O_pow : 
	  astack_d[atop-2]= Math.pow(astack_d[atop-2],astack_d[atop-1]); atop--;
	  break;
	case I_PRF | O_neg : 
	  astack_d[atop-1]= -astack_d[atop-1];
	  break;
	case I_PRF | O_mul : 
	  astack_d[atop-2]= astack_d[atop-2] * astack_d[atop-1]; atop--;
	  break;
	case I_PRF | O_add : 
	  astack_d[atop-2]= astack_d[atop-2] + astack_d[atop-1]; atop--;
	  break;
	case I_PRF | O_sub : 
	  astack_d[atop-2]= astack_d[atop-2] - astack_d[atop-1]; atop--;
	  break;
	case I_PRF | O_div : 
	  astack_d[atop-2]= astack_d[atop-2] / astack_d[atop-1]; atop--;
	  break;
	case I_PRF | O_lt : 
	  astack_d[atop-2]= (astack_d[atop-2] < astack_d[atop-1])?-1:0; atop--;
	  break;
	case I_PRF | O_le : 
	  astack_d[atop-2]= (astack_d[atop-2] <= astack_d[atop-1])?-1:0; atop--;
	  break;
	case I_PRF | O_eq : 
	  astack_d[atop-2]= (astack_d[atop-2] == astack_d[atop-1])?-1:0; atop--;
	  break;
	case I_PRF | O_ge : 
	case I_PRF | O_ge2 : 
	  astack_d[atop-2]= (astack_d[atop-2] >= astack_d[atop-1])?-1:0; atop--;
	  break;
	case I_PRF | O_gt : 
	  astack_d[atop-2]= (astack_d[atop-2] > astack_d[atop-1])?-1:0; atop--;
	  break;
	case I_PRF | O_ne : 
	  astack_d[atop-2]= (astack_d[atop-2] != astack_d[atop-1])?-1:0; atop--;
	  break;
         //if (oper.equals("or")) { answer=(double)(((int)(left)) | ((int)(right))); }
    //else if (oper.equals("xor")) { answer=(double)(((int)(left)) ^ ((int)(right))); }
    //else if (oper.equals("and")) { answer=(double)(((int)(left)) & ((int)(right))); }
    //else if (oper.equals("not")) { answer=(double)(~(int)(right)); }
	case I_PRF | O_not : 
	  astack_d[atop-1]= (double)(~(int)(astack_d[atop-1]));
	  break;
	case I_PRF | O_and : 
	  astack_d[atop-2]= (((int)(astack_d[atop-2])) & ((int)(astack_d[atop-1]))); atop--;
	  break;
	case I_PRF | O_or : 
	  astack_d[atop-2]= (((int)(astack_d[atop-2])) | ((int)(astack_d[atop-1]))); atop--;
	  break;
	case I_PRF | O_xor : 
	  astack_d[atop-2]= (((int)(astack_d[atop-2])) ^ ((int)(astack_d[atop-1]))); atop--;
	  break;

	case I_PRF | T_Str | O_gt : 
	  astack_d[atop-2]= (astack_s[atop-2].compareTo(astack_s[atop-1])>0)?-1:0; atop--;
	  break;
	case I_PRF | T_Str | O_lt : 
	  astack_d[atop-2]= (astack_s[atop-1].compareTo(astack_s[atop-2])>0)?-1:0; atop--;
	  break;
	case I_PRF | T_Str | O_ne : 
	  astack_d[atop-2]= (astack_s[atop-2].equals(astack_s[atop-1]))?0:-1; atop--;
	  break;
	case I_PRF | T_Str | O_eq : 
	  astack_d[atop-2]= (astack_s[atop-2].equals(astack_s[atop-1]))?-1:0; atop--;
	  break;
	case I_PSH | T_Str | M_IMM : 
	  astack_s[atop++]=pargS[i];
	  break;
	case I_PSH | T_Str | M_MEM : 
	  astack_s[atop++]=using_machine.variables.variablestring[pargmem[i]];
	  break;
	case I_PSH | T_Str | M_MEMARR1 : 
	  astack_s[atop-1]=using_machine.variables.variablearraystring1[pargmem[i]][(int)astack_d[atop-1]];
	  break;
	case I_PSH | T_Str | M_MEMARR2 : 
	  astack_s[atop-2]=using_machine.variables.variablearraystring2[pargmem[i]][(int)astack_d[atop-2]][(int)astack_d[atop-1]]; atop--;
	  break;
	case I_STO | T_Str | M_MEM : 
	  using_machine.variables.variablestring[pargmem[i]]=astack_s[--atop];
	  break;
	case I_STO | T_Str | M_MEMARR1 : 
	  using_machine.variables.variablearraystring1[pargmem[i]][(int)astack_d[atop-2]]=astack_s[atop-1];
	  atop--; atop--;
	  break;
	case I_STO | T_Str | M_MEMARR2 : 
	  using_machine.variables.variablearraystring2[pargmem[i]][(int)astack_d[atop-3]][(int)astack_d[atop-2]]=astack_s[atop-1];
	  atop--; atop--; atop--;
	  break;

	case I_PSH | T_Dbl | M_IMM : 
	  astack_d[atop++]=pargD[i];
	  break;
	case I_PSH | T_Dbl | M_MEM : 
	  astack_d[atop++]=using_machine.variables.variablevalue[pargmem[i]];
	  break;
	case I_PSH | T_Dbl | M_MEMARR1 : 
	  astack_d[atop-1]=using_machine.variables.variablearrayvalue1[pargmem[i]][(int)astack_d[atop-1]];
	  break;
	case I_PSH | T_Dbl | M_MEMARR2 : 
	  astack_d[atop-2]=using_machine.variables.variablearrayvalue2[pargmem[i]][(int)astack_d[atop-2]][(int)astack_d[atop-1]];
          atop--;
	  break;
	case I_STO | T_Dbl | M_MEM : 
	  using_machine.variables.variablevalue[pargmem[i]]=astack_d[--atop];
	  break;
	case I_STO | T_Dbl | M_MEMARR1 : 
	  using_machine.variables.variablearrayvalue1[pargmem[i]][(int)astack_d[atop-2]]=astack_d[atop-1];
	  atop--; atop--;
	  break;
	case I_STO | T_Dbl | M_MEMARR2 : 
	  using_machine.variables.variablearrayvalue2[pargmem[i]][(int)astack_d[atop-3]][(int)astack_d[atop-2]]=astack_d[atop-1];
	  atop--; atop--; atop--;
	  break;
	default:
          if (verbose) System.out.printf("X ");
          System.out.printf("Instruction Fault at %d instruction %d\n",i,prog[i]);
	  // should through an error! Instruction Fault
          throw new EvaluateException("INSTRUCTION FAULT");              
	  //break;
      }
      if (verbose) System.out.printf("\n");
    }
          } catch(Exception e) {
	    //System.out.println(e.getMessage());
	    e.printStackTrace();
	    System.out.println(e);
            throw new EvaluateException("BAD SUBSTRING INDEX");              
          }
    
    //return nextpnt(x); // we've gone off the end
    return -1;
  }

  double result()
  {
    // just pop the last result
	  return astack_d[--atop];
  }

  //GenericType resultlist()
  //{
    //GenericType gt; 
	  //// opposite order to pop
	  //// we need to know the type just assume numbers lists only for now...
     //for(i=0; i<atop; ++i) {
     //}
     //return gt;
  //}

  //GenericType result() {
    //if (verbose) { System.out.printf("upto=%d\n",upto); }
    //GenericType gt; //only allow list of 
    //if (stktype[0]==ST_NUM) {
      //gt=new GenericType(stknum[0]);
    //} else {
      //if (verbose) { System.out.printf("Returning a string type from evaluate\n"); }
      //gt=new GenericType(stkstring[0]);
    //}
    //// if there are more, add to it
    //for (int i=1; i<upto; ++i) {
      //if (stktype[i]==ST_NUM) {
        //gt.add(stknum[i],upto);
      //} else {
        //gt.add(stkstring[i],upto);
      //}
    //}
    //return gt;
  //}
  //////////////////////////////////////////////////////////////////////
  ///
  //////////////////////////////////////////////////////////////////////

  int nextpnt(int x) { return acpointer_next[x]; }
  int savestart_p=0;
  int savestart_ac=0;
  boolean is_compiled(int x) { return acpointer[x]>0; }
  void savestart(int x) { 
	  // if <0 then it is a rejected compile, so dont try to recompile, just nod and carry on...
	  if (acpointer[x]<0) return;
	  savestart_p=x; savestart_ac=top; tmptop=top; 
	  record=true; 
          using_machine.evaluate_engine.speeder_compile=true;
      // probably dont need both record and swith on speeder_compile
  }
  void saveelseacode(int then, int end) {
    acpointer_next[then]=end;
  }
  void saveacode(int end) 
  {
	  if (record) {
	    // push a HLT onto code
	    addInstr(I_HLT); 
	    acpointer[savestart_p]=savestart_ac;
	    acpointer_next[savestart_p]=end;
	    // this is now marked as a valid piece of code
	    top=tmptop;
            if (using_machine.verbose) System.out.printf("Saved code from %d to %d for location %d\n",savestart_ac,top,savestart_p);
	    record=false;
            using_machine.evaluate_engine.speeder_compile=false;
	  }
  }

  void reject()
  {
    acpointer[savestart_p]=-1; // don't try to run acode or recompile this
    record=false;
    using_machine.evaluate_engine.speeder_compile=false;
  }

  // Instruction Type Mode Operation Function
  static final int I_PRF=0<<6; // was 8
  static final int I_PSH=1<<6;
  static final int I_STO=2<<6;
  static final int I_FNC=3<<6;
  //static final int I_HLT=4<<6; // this could be turned into FNC HLT and would save a bit!


  static final int T_Dbl=0<<5; // was 7
  static final int T_Str=1<<5;

  static final int M_IMM=0<<0; // was 5, then 3
  static final int M_MEM=1<<0;
  static final int M_MEMARR1=2<<0;
  static final int M_MEMARR2=3<<0;

  // can overflow M .: 5 bits = 32 vals
  static final int O_PARAM=0;
  static final int O_pow=1;
  static final int O_mul=2;
  static final int O_div=3;
  static final int O_add=4;
  static final int O_sub=5;
  static final int O_neg=6;
  static final int O_not=7;
  static final int O_and=8;
  static final int O_or=9;
  static final int O_xor=10;
  static final int O_eq=11;
  static final int O_lt=12;
  static final int O_gt=13;
  static final int O_ge=14;
  static final int O_le=15;
  static final int O_ne=16;
  static final int O_ge2=17;

  // these can overflow into the M bits and T, which aren't used for FNC .: 6 bits 64 vals
  static final int F_HLT=0;
  static final int I_HLT=I_FNC | F_HLT; 
  static final int F_sin=1;
  static final int F_cos=2;
  static final int F_int=3;
  static final int F_log=4;
  static final int F_sqr=5;
  static final int F_sqrt=6;
  static final int F_atn=7;
  static final int F_tan=8;
  static final int F_asin=9;
  static final int F_acos=10;
  static final int F_abs=11;
  static final int F_rnd=12;
  static final int F_exp=13;
  static final int F_sgn=14;
  static final int F_len=15;
  static final int F_val=16;
  static final int F_asc=17;
  static final int F_midD=18;
  static final int F_leftD=19;
  static final int F_rightD=20;
  static final int F_strD=21;
  static final int F_chrD=22;
  static final int F_instr=23;
  static final int F_var_ti=24;
  static final int F_var_st=25;
  static final int F_var_tiD=26;
  static final int F_var_pi=27;
  static final int F_peek=28;
  static final int F_fre=29;
  static final int F_pos=30;
  static final int F_tab=31;
  static final int F_spc=32;
  static final int F_frm=33;

  static String O_strings[]={"XXXXXX","^","*","/","+","-","-ve","not","and","or","xor","=","<",">",">=","<=","<>","=>"};
  static String F_strings[]={"HLTXXX","sin","cos","int","log","sqr","sqrt","atn","tan","asin","acos","abs","rnd","exp","sgn",
	                     "len","val","asc","mid$","left$","right$","str$","chr$","instr",
                             "ti","st","ti$","mathpi",
                             "peek","fre","pos","tab","spc","frm"};
  //enum { I_PRF, I_PSH, I_STO, I_FNC, I_HLT };           //0..5  (3 bits)

  // enum { T_Dbl, T_Str };                                //0..1  (1 bit)
  // enum { M_IMM, M_MEM, M_MEMARR1, M_MEMARR2 };          //0..3  (2 bits)
  // enum { O_exp, O_mul, O_div, O_add, O_sub, O_neg       //0..15 (4 bits)
  //     ,O_not,O_and,O_or,O_xor
  //       ,O_eq,O_lt,O_gt,O_ge,O_le
  //};
  //enum { F_sin, F_cos, F_int }                         //0..2  but will be 36 - so (5 bits)

}
