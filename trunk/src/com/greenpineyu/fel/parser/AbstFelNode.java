package com.greenpineyu.fel.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.apache.commons.lang.ObjectUtils.Null;

import com.greenpineyu.fel.compile.FelMethod;
import com.greenpineyu.fel.context.FelContext;
import com.greenpineyu.fel.interpreter.Interpreter;

public abstract class AbstFelNode extends CommonTree implements FelNode, Interpreter {

	/**
	 * 解析器,用于解析节点的值
	 */
	protected Interpreter interpreter;


	/**
	 * 点位节点
	 */
	public static final AbstFelNode NULL_NODE = new AbstFelNode() {
		public boolean isNil() {
			return false;
		};

		public Object interpret(FelContext context, FelNode node) {
			return null;
		};
		
		public String toString() {return "NULL_NODE";}

		public FelMethod toMethod(FelContext ctx) {
			throw new UnsupportedOperationException("占位节点");
		}

	};
	
	public static final AbstFelNode NULL = new AbstFelNode() {
		public boolean isNil() {
			return false;
		};
		
		public Object interpret(FelContext context, FelNode node) {
			return null;
		};
		
		public String getText() {
			return "null";
		};
		
		
		public String toString() {return "NULL_NODE";}
		
		public FelMethod toMethod(FelContext ctx) {
			return new FelMethod(Null.class, "null");
		}
		
	};
	
	public static final FelNode TRUE_NODE = new AbstFelNode(){
		public boolean isNil() {
			return false;
		};
		
		public Object interpret(FelContext context, FelNode node) {
			return Boolean.TRUE;
		}

		public FelMethod toMethod(FelContext ctx) {
			return new FelMethod(Boolean.class, "true");
		}


	};


	public AbstFelNode(Token token) {
		super(token);
	}

	public AbstFelNode() {

	}

	{
		//解释器设置成自己
		resetInterpreter();
	}


	public AbstFelNode(CommonTree node) {
		super(node);
		if (node.getChildren() != null) {
			this.children = new ArrayList(node.getChildren());
			for (int i = 0; i < this.children.size(); i++) {
				Tree object = (Tree) this.children.get(i);
				object.setParent(this);
			}
		}
	}


	public String toString() {
		return this.getText();
	}




	public void setChild(int index, FelNode node) {
		if (node instanceof Tree) {
			super.setChild(index, (Tree) node);
		} else {
			throw new IllegalArgumentException("类型必须是" + Tree.class);
		}

	}



	public Object eval(FelContext context) {
		return interpreter.interpret(context, this);
		//		if (cached) {
		//			return cacheValue;
		//		}
		//		Object eval = interpreter.interpret(context, this);
		//		if (fixed) {
		//			cacheValue = eval;
		//			cached = true;
		//		}
		//		return eval;
	}

	//	abstract public Object evalWithoutCache(FelContext context);


	public static List getNodes(FelNode node) {
		List returnMe = new ArrayList();
		getNodes(node, returnMe);
		return returnMe;
	}

	public static void getNodes(FelNode node, List returnMe) {
		if (node != null) {
			returnMe.add(node);
			List nodeChildren = node.getChildren();
			if (nodeChildren != null) {
				for (Iterator iterator = nodeChildren.iterator(); iterator.hasNext();) {
					try {
						FelNode child = (FelNode) iterator.next();
						getNodes(child, returnMe);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private Object[] childrenCache;

	public Object[] getChildrenArray() {
		if (childrenCache != null) {
			return childrenCache;
		}
		if (children == null) {
			childrenCache = new Object[0];
			return childrenCache;
		}
		childrenCache = new Object[children.size()];
		for (int i = 0; i < childrenCache.length; i++) {
			childrenCache[i] = (Tree) children.get(i);
		}
		return childrenCache;
	}

	public Interpreter getInterpreter() {
		return this.interpreter;
	}

	public void setInterpreter(Interpreter interpreter) {
		this.interpreter = interpreter;
	}

	public void resetInterpreter() {
		this.interpreter = this;
	}

	public Object interpret(FelContext context, FelNode node) {
		throw new UnsupportedOperationException("还没有实现[2011-1-13]");
	}

}