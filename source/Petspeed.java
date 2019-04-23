////////////////////////////////////////////////
//
// Petspeed 'er
// Goes much (26x) faster
//  now...
// Goes much (50x) faster (on the fastest machine)
//
//       REM skip
//       next fix
// Opt09 eliminate param marking on stack for assignment : 10% faster
// Opt10 chaining : 16-30% faster again
//      .. todo - i think every jmp should just be a NOP
// Opt11 NOP, ordered instr -> no increase/decrease - but NOP makes more sense
// Opt   use vv internal 
// Opt   IF return values - avoid creating tmp GenericType (just use int)
// Opt   NEXT no more string usage
// Opt15 (DEF) FN now in-line compiled (was not even compiled at all before)
// Opt16 chew NOP goes from 7500 to 8000 mp2 = 6.7%
// Opt17 detect and short circuit boring time consuming extra jumps
// 
////////////////////////////////////////////////
class Petspeed
{
  static final int MAX=5000;
  // store program memory
  int top=1; // because 0 is a special pointer to show invalid
  int tmptop;
  int tmptoprev;
  boolean record=false;
  int []prog= new int[MAX]; // program memory instruction
  double [] pargD = new double[MAX]; // double arg
  int [] pargmem = new int[MAX]; // mem pointer arg
  String [] pargS = new String[MAX]; // string arg
  Machine using_machine=null;

  static final int MAXPSIZE=50000; // just for now, have a more efficient sparse array later
  int []acpointer = new int[MAXPSIZE];
  int []acpointer_next = new int[MAXPSIZE];   // fixme - maybe this could go on HLT parameter

  // to fix -> make this take less space! we are now taking 250k!
  int []pnext = new int[MAXPSIZE];
  int []pcache = new int[MAXPSIZE];
  int []btpnt = new int[MAXPSIZE]; // only used for basictimer


  ////////////////////////////////////////////////////////////////
  //
  ////////////////////////////////////////////////////////////////
  boolean verbose=false; //shorthand quicker

  double []vv;

  Petspeed(Machine m) {
    using_machine=m;
    vv=using_machine.variables.variablevalue;
  }
  // storing the program
  int addInstr(int instr) { return addInstr(instr, 0.0,-1, ""); }
  int addInstr(int instr, String argS) { return addInstr(instr, 0.0,-1, argS); }
  int addInstr(int instr, double argD, int argmem) { return addInstr(instr, argD, argmem, ""); }
  int addInstr(int instr, double argD) { return addInstr(instr, argD, -1, ""); }
  int addInstr(int instr, int argmem)  { return addInstr(instr, 0.0, argmem, ""); }
  int addInstr(int instr, double argD, int argmem, String argS) {
    verbose=using_machine.verbose; //better spot -moreeffecient
    if (record) {
      prog[tmptop]=instr;
      pargD[tmptop]=argD;
      pargmem[tmptop]=argmem;
      pargS[tmptop]=argS;
      if (verbose) System.out.printf("%d %d %d %f %s\n",tmptop,prog[tmptop],pargmem[tmptop],pargD[tmptop],pargS[tmptop]);
      tmptop++;
      return 1;
    } else return 0;
  }
  void rewind() {
    tmptop--;
  }
   
  void saverev() {
    tmptoprev=tmptop;
  }
  void reverseSTO() {
    int i,j;
    // work from bottom and top evenly
    i=tmptoprev;
    j=tmptop-1;
    while(i<j) {
      while ((prog[i]&(64+128))!=I_STO && i<j) i++;
      while ((prog[j]&(64+128))!=I_STO && i<j) j++;
      if (i<j) {
        // swap them
        int    tmpprog=prog[i];
        double tmppargD=pargD[i];
        int    tmppargmem=pargmem[i];
        String tmppargS=pargS[i];
	prog[i]=prog[j];
	pargD[i]=pargD[j];
	pargmem[i]=pargmem[j];
	pargS[i]=pargS[j];
        prog[j]=tmpprog;
        pargD[j]=tmppargD;
	pargmem[j]=tmppargmem;
	pargS[j]=tmppargS;
	if (verbose) { System.out.printf("A-COMPILER switch STO i=%d j=%d\n",i,j); }
	i++; j--;
      } else break;
    }
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
  int gatop=0; // no advantage byte - does an extra i2b

  //int listtop=0; // no advantage byte - does an extra i2b
  GenericType list;

  void listadd() {
    if (gatop==0) list=new GenericType(false);
  }
  void listadd(double d) {
    if (gatop==0) list=new GenericType(d);
    else list.add(d,20); // hard code 20 for now FIX
    gatop++;
  }

  void listadd(String s) {
    if (gatop==0) list=new GenericType(s);
    else list.add(s,20); // hard code 20 for now FIX
    gatop++;
  }

  java.text.SimpleDateFormat localDateFormat = new java.text.SimpleDateFormat("HHmmss"); // for TI$ efficiency

  // FIX could replace listtop by gatop
  ///////////////////////////////////////////////////////////////////////
  //
  // the executor
  //
  //
  ///////////////////////////////////////////////////////////////////////
  int execute(int x) throws EvaluateException,BasicException {
    int atop=gatop;
    if (atop!=0) throw new EvaluateException("STACK CREEP");
    //listtop=atop; // extra overhead - want to avoid this if can - FIX

    try { // safety catch!
    for (int i=acpointer[x]; /* i<MAX*/true; ++i) {
      //if (prog[i]==I_HLT) break;
      /// just for now to gain 5.5% speed// if (verbose) System.out.printf("\nEXECUTING %d %d %d %f %s  ",i,prog[i],pargmem[i],pargD[i],pargS[i]);
      switch(prog[i]) {
        case I_HLT: 
          if (verbose) {
            System.out.printf("\n");
	    if (verbose && gatop>0) { System.out.printf("pushed %d onto gt list\n",gatop); }
	  }
	  if (gatop==0 && atop!=0 && atop!=1) System.out.printf("atop not at zero or one (%d)\n",atop);
	  //if (verbose) { System.out.printf("about to return nextpnt(x)= %d\n",nextpnt(x)); }
	  gatop=atop-gatop; //? CHECK
	  return nextpnt(x);
	case I_PRF | T_Str :
          //speedup// if (verbose) System.out.printf("Return parameter %d flagged as a string stack=%d ",gatop,atop);
          listadd(astack_s[gatop]);
	  break;
	case I_PRF | T_Dbl :
          //speedup// if (verbose) System.out.printf("Return parameter %d flagged as a double stack=%d ",gatop,atop);
          listadd(astack_d[gatop]);
	  break;
	case I_PRF | O_EMPTY :
          //listadd();
          if (gatop==0) list=new GenericType(false);
	  break;

	// most common at top
	case I_FNC | F_NOP : // special Opt10 - don't really have to do this - just to make it a bit nicer
	  continue;
	case I_PSH | T_Dbl | M_IMM : 
	  astack_d[atop++]=pargD[i];
	  break;
	case I_PSH | T_Dbl | M_MEM : 
	  //astack_d[atop++]=using_machine.variables.variablevalue[pargmem[i]];
	  astack_d[atop++]=vv[pargmem[i]];
	  continue;
	case I_PRF | O_mul : 
	  //astack_d[atop-2]= astack_d[atop-2] * astack_d[atop-1]; atop--;
	  astack_d[--atop-1]*= astack_d[atop];
	  continue;
	case I_PRF | O_add : 
	  //astack_d[atop-2]= astack_d[atop-2] + astack_d[atop-1]; atop--;
	  astack_d[--atop-1]+= astack_d[atop];
	  continue;
	case I_PRF | O_sub : 
	  //astack_d[atop-2]= astack_d[atop-2] - astack_d[atop-1]; atop--;
	  astack_d[--atop-1]-= astack_d[atop];
	  continue;
	case I_PRF | O_div : 
	  //astack_d[atop-2]= astack_d[atop-2] / astack_d[atop-1]; atop--;
	  astack_d[--atop-1]/= astack_d[atop];
	  continue;
	case I_STO | T_Dbl | M_MEM : 
	  //using_machine.variables.variablevalue[pargmem[i]]=astack_d[--atop];
	  vv[pargmem[i]]=astack_d[--atop];
	  continue;

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
	    gatop=atop;
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
	    gatop=atop;
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
	    gatop=atop;
            throw new EvaluateException("BAD SUBSTRING INDEX");              
          }
	  break;
	case I_FNC | F_strD : 
	  // I think the leading space is wrong - it should be -ve if it is.... FIX
	  if (astack_d[atop-1]-(int)astack_d[atop-1]==0.0) {
	    astack_s[atop-1]=((astack_d[atop-1]<0.0)?"":" ")+(int)astack_d[atop-1];
          } else {
	    astack_s[atop-1]=((astack_d[atop-1]<0.0)?"":" ")+(new Double(astack_d[atop-1]).toString());
          }
	  break;
	case I_FNC | F_len : 
          try {          
	    astack_d[atop-1]=astack_s[atop-1].length();
          } catch(Exception e) {
	    gatop=atop;
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
            if (astack_s[atop-1]=="") {
	      gatop=atop;
              throw new EvaluateException("ILLEGAL QUANTITY");
	    }
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
	  astack_d[atop++]=using_machine.fileio_ST; using_machine.fileio_ST=0;
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

	case I_PSH | T_Dbl | M_MEMARR1 : 
	  astack_d[atop-1]=using_machine.variables.variablearrayvalue1[pargmem[i]][(int)astack_d[atop-1]];
	  break;
	case I_PSH | T_Dbl | M_MEMARR2 : 
	  astack_d[atop-2]=using_machine.variables.variablearrayvalue2[pargmem[i]][(int)astack_d[atop-2]][(int)astack_d[atop-1]];
          atop--;
	  break;
	case I_STO | T_Dbl | M_MEMARR1 : 
	  using_machine.variables.variablearrayvalue1[pargmem[i]][(int)astack_d[atop-2]]=astack_d[atop-1];
	  atop--; atop--;
	  break;
	case I_STO | T_Dbl | M_MEMARR2 : 
	  using_machine.variables.variablearrayvalue2[pargmem[i]][(int)astack_d[atop-3]][(int)astack_d[atop-2]]=astack_d[atop-1];
	  atop--; atop--; atop--;
	  break;
	case I_FNC | F_JMP : // special Opt10
	  i=pargmem[i]-1; // goto / jmp
	  continue;
	  /**
	case I_FNC | F_JSR :
	  astack_d[atop++]=i+1; // push next pc // better to be int
	  i=pargmem[i]-1; // goto / jmp
	  continue;
	case I_FNC | F_RTN : // special Opt10
	  i=(int)astack_d[--atop]; // pop the pc // better to be int
	  continue;
	   **/
 	case I_NXT : 
	  { int p;
	    if ((p=using_machine.processNEXTspeeder(pargmem[i]))>=0) {
              // jump
              return p;
	    }
	  }
	  break; // just go to next instruction

	  // just a speed test
	//case 250 : break;
	//case 251 : break;
	//case 252 : break;
	//case 253 : break;
	//case 254 : break;
	//case 255 : break;

	default:
          if (verbose) System.out.printf("X ");
          System.out.printf("Instruction Fault at %d instruction %d\n",i,prog[i]);
	  // should through an error! Instruction Fault
	  gatop=atop;
          throw new EvaluateException("INSTRUCTION FAULT");              
	  //break;
      } // case
    } // for
      } catch (BasicLineNotFoundError e) { 
        e.printStackTrace();
	gatop=atop;
        throw e;
      } catch (ArrayIndexOutOfBoundsException e) { 
        e.printStackTrace();
	gatop=atop;
        throw new EvaluateException("ILLEGAL QUANTITIY");              
      } catch(Exception e) {
	//System.out.println(e.getMessage());
	e.printStackTrace();
	System.out.println(e);
	gatop=atop;
        throw new EvaluateException("EXECUTE ERROR");              
      }
    
    //return nextpnt(x); // we've gone off the end
    // return -1; // don't get here now as we let it blow the array (catch will get it)
  }

  int execute2(int x) throws EvaluateException,BasicException {
    int atop=gatop;
    if (atop!=0) throw new EvaluateException("STACK CREEP");
    //listtop=atop; // extra overhead - want to avoid this if can - FIX

    try { // safety catch!
    for (int i=acpointer[x]; /* i<MAX*/true; ++i) {
      //if (prog[i]==I_HLT) break;
      /// just for now to gain 5.5% speed// if (verbose) System.out.printf("\nEXECUTING %d %d %d %f %s  ",i,prog[i],pargmem[i],pargD[i],pargS[i]);
      switch(prog[i]) {
        case I_HLT: 
          if (verbose) {
            System.out.printf("\n");
	    if (verbose && gatop>0) { System.out.printf("pushed %d onto gt list\n",gatop); }
	  }
	  if (gatop==0 && atop!=0 && atop!=1) System.out.printf("atop not at zero or one (%d)\n",atop);
	  //if (verbose) { System.out.printf("about to return nextpnt(x)= %d\n",nextpnt(x)); }
	  gatop=atop-gatop; //? CHECK
	  return nextpnt(x);
	case I_PRF | T_Str :
          //speedup// if (verbose) System.out.printf("Return parameter %d flagged as a string stack=%d ",gatop,atop);
          listadd(astack_s[gatop]);
	  break;
	case I_PRF | T_Dbl :
          //speedup// if (verbose) System.out.printf("Return parameter %d flagged as a double stack=%d ",gatop,atop);
          listadd(astack_d[gatop]);
	  break;
	case I_PRF | O_EMPTY :
          //listadd();
          if (gatop==0) list=new GenericType(false);
	  break;

	// most common at top
	case I_FNC | F_NOP : // special Opt10 - don't really have to do this - just to make it a bit nicer
	  continue;
	case I_PSH | T_Dbl | M_IMM : 
	  astack_d[atop++]=pargD[i];
	  break;
	case I_PSH | T_Dbl | M_MEM : 
	  //astack_d[atop++]=using_machine.variables.variablevalue[pargmem[i]];
	  astack_d[atop++]=vv[pargmem[i]];
	  continue;
	case I_PRF | O_mul : 
	  //astack_d[atop-2]= astack_d[atop-2] * astack_d[atop-1]; atop--;
	  astack_d[--atop-1]*= astack_d[atop];
	  continue;
	case I_PRF | O_add : 
	  //astack_d[atop-2]= astack_d[atop-2] + astack_d[atop-1]; atop--;
	  astack_d[--atop-1]+= astack_d[atop];
	  continue;
	case I_PRF | O_sub : 
	  //astack_d[atop-2]= astack_d[atop-2] - astack_d[atop-1]; atop--;
	  astack_d[--atop-1]-= astack_d[atop];
	  continue;
	case I_PRF | O_div : 
	  //astack_d[atop-2]= astack_d[atop-2] / astack_d[atop-1]; atop--;
	  astack_d[--atop-1]/= astack_d[atop];
	  continue;
	case I_STO | T_Dbl | M_MEM : 
	  //using_machine.variables.variablevalue[pargmem[i]]=astack_d[--atop];
	  vv[pargmem[i]]=astack_d[--atop];
	  continue;

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
	    gatop=atop;
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
	    gatop=atop;
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
	    gatop=atop;
            throw new EvaluateException("BAD SUBSTRING INDEX");              
          }
	  break;
	case I_FNC | F_strD : 
	  // I think the leading space is wrong - it should be -ve if it is.... FIX
	  if (astack_d[atop-1]-(int)astack_d[atop-1]==0.0) {
	    astack_s[atop-1]=((astack_d[atop-1]<0.0)?"":" ")+(int)astack_d[atop-1];
          } else {
	    astack_s[atop-1]=((astack_d[atop-1]<0.0)?"":" ")+(new Double(astack_d[atop-1]).toString());
          }
	  break;
	case I_FNC | F_len : 
          try {          
	    astack_d[atop-1]=astack_s[atop-1].length();
          } catch(Exception e) {
	    gatop=atop;
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
            if (astack_s[atop-1]=="") {
	      gatop=atop;
              throw new EvaluateException("ILLEGAL QUANTITY");
	    }
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
	  astack_d[atop++]=using_machine.fileio_ST; using_machine.fileio_ST=0;
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

	case I_PSH | T_Dbl | M_MEMARR1 : 
	  astack_d[atop-1]=using_machine.variables.variablearrayvalue1[pargmem[i]][(int)astack_d[atop-1]];
	  break;
	case I_PSH | T_Dbl | M_MEMARR2 : 
	  astack_d[atop-2]=using_machine.variables.variablearrayvalue2[pargmem[i]][(int)astack_d[atop-2]][(int)astack_d[atop-1]];
          atop--;
	  break;
	case I_STO | T_Dbl | M_MEMARR1 : 
	  using_machine.variables.variablearrayvalue1[pargmem[i]][(int)astack_d[atop-2]]=astack_d[atop-1];
	  atop--; atop--;
	  break;
	case I_STO | T_Dbl | M_MEMARR2 : 
	  using_machine.variables.variablearrayvalue2[pargmem[i]][(int)astack_d[atop-3]][(int)astack_d[atop-2]]=astack_d[atop-1];
	  atop--; atop--; atop--;
	  break;
	case I_FNC | F_JMP : // special Opt10
	  i=pargmem[i]-1; // goto / jmp
	  continue;
	  /**
	case I_FNC | F_JSR :
	  astack_d[atop++]=i+1; // push next pc // better to be int
	  i=pargmem[i]-1; // goto / jmp
	  continue;
	case I_FNC | F_RTN : // special Opt10
	  i=(int)astack_d[--atop]; // pop the pc // better to be int
	  continue;
	   **/
 	case I_NXT : 
	  { int p;
	    if ((p=using_machine.processNEXTspeeder(pargmem[i]))>=0) {
              // jump
              return p;
	    }
	  }
	  break; // just go to next instruction

	  // just a speed test
	//case 250 : break;
	//case 251 : break;
	//case 252 : break;
	//case 253 : break;
	//case 254 : break;
	//case 255 : break;

	default:
          if (verbose) System.out.printf("X ");
          System.out.printf("Instruction Fault at %d instruction %d\n",i,prog[i]);
	  // should through an error! Instruction Fault
	  gatop=atop;
          throw new EvaluateException("INSTRUCTION FAULT");              
	  //break;
      } // case
    } // for
      } catch (BasicLineNotFoundError e) { 
        e.printStackTrace();
	gatop=atop;
        throw e;
      } catch (ArrayIndexOutOfBoundsException e) { 
        e.printStackTrace();
	gatop=atop;
        throw new EvaluateException("ILLEGAL QUANTITIY");              
      } catch(Exception e) {
	//System.out.println(e.getMessage());
	e.printStackTrace();
	System.out.println(e);
	gatop=atop;
        throw new EvaluateException("EXECUTE ERROR");              
      }
    
    //return nextpnt(x); // we've gone off the end
    // return -1; // don't get here now as we let it blow the array (catch will get it)
  }

  void pushEmpty() {
          addInstr(Petspeed.I_PRF | Petspeed.O_EMPTY); // flag a double
  }

  void pushD() {
          addInstr(Petspeed.I_PRF | Petspeed.T_Dbl); // flag a double
  }

  void pushS() {
          addInstr(Petspeed.I_PRF | Petspeed.T_Str); // flag a string
  }

  //String result(boolean isString)
  //{
    //// just pop the last result
	  //return astack_s[--gatop];
  //}

  double result()
  {
    // just pop the last result
	  return astack_d[--gatop];
  }

  //////////////////////////////////////////////////////////////////////
  ///
  //////////////////////////////////////////////////////////////////////

  int nextpnt(int x) { return acpointer_next[x]; }
  int savestart_p=0;
  int savestart_ac=0;
  int lastassign=-1; // Opt10
  int lastfinal=-1; // Opt10
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


  void saveacode(int end) {
	  if (record) {
	    // push a HLT onto code
	    addInstr(I_HLT); 
	    acpointer[savestart_p]=savestart_ac;
	    acpointer_next[savestart_p]=end;
    // Opt17  // not used just yet
    //NOTUSEDYET//if (acpointer_next[end]<=0) acpointer_next[end]=-savestart_p; // back link
	    // this is now marked as a valid piece of code
	    top=tmptop;
            if (verbose) System.out.printf("Saved code from %d to %d for location %d\n",savestart_ac,top,savestart_p);
	    record=false;
            using_machine.evaluate_engine.speeder_compile=false;
	    // Opt10
	    // opt10
	    // have a look to see if you can chain this
	    if (lastassign>=0) {
              int p=acpointer_next[lastassign];
	      if (p>=0) {
                if (pcache[p]==1 /* assign */ && pnext[p]==savestart_p) {
                  if (verbose) System.out.printf("We can chain!\n");
                  if (verbose) System.out.printf("want to change %d from HLT to GOTO %d\n",lastfinal,savestart_ac);
		  if (lastfinal+1==savestart_ac) {
                    prog[lastfinal]=I_FNC|F_NOP; // dont have to - but just to make it look a bit nicer
		    // crunch up the nop! (new Opt)
		    for (int i=lastfinal; i<top-1; ++i) {
                      prog[i]=prog[i+1];
		      pargD[i]=pargD[i+1];
		      pargmem[i]=pargmem[i+1];
		      pargS[i]=pargS[i+1];
		    }
		    top--;
		    acpointer[savestart_p]=savestart_ac-1; // I think I need to do this for those that jump half way in CHECK
		    acpointer_next[savestart_p]=lastassign+1; // FixOpt10=> if you jump midway in, go to the first+1 which will jump to the real end
		  } else {
                    prog[lastfinal]=I_FNC|F_JMP; pargmem[lastfinal]=savestart_ac;
		  }
		  acpointer_next[lastassign]=end; // combine
		  pnext[lastassign+1]=end; // FixOpt10 => always keep the real end updated 
		  pcache[lastassign+1]=33 /* REM */ /* FIX */; // FixOpt10 => always keep the real end updated - pretend it is a REM
    // Opt17  // not used just yet
    //NOTUSEDYET//if (acpointer_next[end]<=0) acpointer_next[end]=-lastassign; // back link
	          lastfinal=top-1;
		  return; // because we want to keep last assign at the original one!
		}
	      }
	    }

	    lastassign=savestart_p;
	    lastfinal=top-1;
	  }
  }

  void reject() {
    acpointer[savestart_p]=-1; // don't try to run acode or recompile this
    record=false;
    using_machine.evaluate_engine.speeder_compile=false;
    lastassign=-1; //Opt10
  }

  void reoptimise() {
	  // look for REM chains
	  int p;
    for (int i=0; i<MAXPSIZE; ++i) if ((p=acpointer_next[i])>0) 
	    while (p>0 && (pcache[p]==0 && pnext[p]>0 || pcache[p]==33 /* REM *//*FIX*/)) {
              acpointer_next[i]=pnext[p];
              p=acpointer_next[i];
            }
  }
  //////////////////////////////////////////////////////////////
  //
  // debugging
  //
  //////////////////////////////////////////////////////////////
  void dumpstate() {
	  String tmpprog=using_machine.programText+"xxxxxx";
    System.out.printf("i acpointer acpointer_next pnext pcache btpnt\n");
    for (int i=0; i<MAXPSIZE; ++i) {
      if (acpointer[i]!=0 || acpointer_next[i]!=0 || pnext[i]!=0 || pcache[i]!=0 || btpnt[i]!=0) {
        System.out.printf("  [%5d] %5d %5d %5d %5d %5d  %s\n",
          i,
	  acpointer[i],acpointer_next[i],pnext[i],pcache[i],btpnt[i],
          tmpprog.substring(i,i+6).replace("\n","[CR]").replace(" ","[SPC]")
	  );
      }
    }
    System.out.printf("acode program\n");
    for (int i=1; i<top; ++i) {
      String ass="";
      int x=prog[i];
      int c=(x&(3<<6))>>6;
      switch (c) {
        case I_PRF>>6:
		ass=I_strings[c]+" "+O_strings[x&31]+" "+((x&32)>0?"Str ":((x&31)==0?"Dbl ":""));
		//ass=I_strings[c]+" "+(x&31);
		// add type 5th bit
		break;
	case I_PSH>>6:
		ass=I_strings[c]+" "+M_strings[x&3]+" "+((x&32)>0?"Str ":"");
		if ((x&3)!=M_IMM) ass+=using_machine.variables.variablename[pargmem[i]];
		// add type 5th bit
		break;
	case I_STO>>6:
		ass=I_strings[c]+" "+M_strings[x&3]+" "+((x&32)>0?"Str ":"");
		if ((x&3)!=M_IMM) ass+=using_machine.variables.variablename[pargmem[i]];
		// add type 5th bit
		break;
	case I_FNC>>6:
		ass=I_strings[c]+" "+F_strings[x&63];
		break;
      }
      //ass=I_Strings[x&(3<<6)>>6]+" ";
      System.out.printf("  .%5d %3d %-18s %3d %f\n",i,prog[i],ass,pargmem[i],pargD[i]);
      if (x==I_HLT) System.out.printf("--\n");
    }
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
  static final int O_EMPTY=18;

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
  static final int F_NOP=34;
  static final int F_JMP=35;
  static final int F_NXT=36;
  static final int I_NXT=I_FNC | F_NXT; 
  //static final int F_JSR=36;
  //static final int F_RTN=37;

  static String I_strings[]={"PRF","PSH","STO","FNC"}; // just for debugging only
  static String M_strings[]={"IMM","MEM","MEMARR1","MEMARR2"}; // just for debugging only

  static String O_strings[]={"XPARAMX","^","*","/","+","-","-ve","not","and","or","xor","=","<",">",">=","<=","<>","=>","NULL"};
  static String F_strings[]={"HLT___","sin","cos","int","log","sqr","sqrt","atn","tan","asin","acos","abs","rnd","exp","sgn",
	                     "len","val","asc","mid$","left$","right$","str$","chr$","instr",
                             "ti","st","ti$","mathpi",
                             "peek","fre","pos","tab","spc","frm","NOP___","JMP___","NXT__" /*,"JSR___","RTN___" */ };
  //enum { I_PRF, I_PSH, I_STO, I_FNC, I_HLT };           //0..5  (3 bits)

  // enum { T_Dbl, T_Str };                                //0..1  (1 bit)
  // enum { M_IMM, M_MEM, M_MEMARR1, M_MEMARR2 };          //0..3  (2 bits)
  // enum { O_exp, O_mul, O_div, O_add, O_sub, O_neg       //0..15 (4 bits)
  //     ,O_not,O_and,O_or,O_xor
  //       ,O_eq,O_lt,O_gt,O_ge,O_le
  //};
  //enum { F_sin, F_cos, F_int }                         //0..2  but will be 36 - so (5 bits)

}
