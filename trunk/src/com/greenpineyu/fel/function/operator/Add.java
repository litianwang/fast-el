package com.greenpineyu.fel.function.operator;

import static com.greenpineyu.fel.common.NumberUtil.toDouble;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.greenpineyu.fel.common.NumberUtil;
import com.greenpineyu.fel.compile.FelMethod;
import com.greenpineyu.fel.compile.SourceBuilder;
import com.greenpineyu.fel.context.FelContext;
import com.greenpineyu.fel.parser.FelNode;

public class Add extends StableFunction  {

	private static Add instance;

	static {
		instance = new Add();
	}

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	private Add() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see .script.function.Function#call(.script.AstNode,
	 * .script.context.ScriptContext)
	 */
	@Override
	public Object call(FelNode node, FelContext context) {
		Object returnMe = null;
		boolean isAdd = node.getText().equals("+");
		for (Iterator<FelNode> iterator = node.getChildren().iterator(); iterator
				.hasNext();) {
			Object child = iterator.next();
			if (child instanceof FelNode) {
				FelNode childNode = (FelNode) child;
				child = childNode.eval(context);
			}
			if (child instanceof String) {
				if (returnMe == null) {
					returnMe = child;
					continue;
				}
				if (isAdd) {
					returnMe = returnMe + (String) child;
				} else {
					throw new IllegalStateException("calc " + node + " error!");
				}
			}
			if (child instanceof Number) {
				if (returnMe == null) {
					returnMe = child;
					continue;
				}
				Number value = (Number) child;
				if (returnMe instanceof Number) {
					Number r = (Number) returnMe;
					returnMe = new Double(isAdd ? toDouble(r)
							+ toDouble(value):toDouble(r)
							- toDouble(value));
				}else if(returnMe instanceof String){
					String r = (String) returnMe;
					returnMe=r+value;
				}
			}
		}
		if(returnMe instanceof Number){
			return NumberUtil.parseNumber(returnMe.toString());
		}
		return returnMe;
	}

	public static Add getInstance() {
		return instance;
	}

	@Override
	public String getName() {
		return "+";
	}
/*
	public String toJavaSrc(FelNode node, FelContext context) {
		List<FelNode> children = node.getChildren();
		StringBuilder sb = new StringBuilder();

		for (FelNode n : children) {
			sb.append("(");
			sb.append(n.toJavaSrc(null));
			sb.append(")");
			sb.append("+");
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}*/

	@Override
	public FelMethod toMethod(FelNode node, FelContext ctx) {
		Class<?> type = null;
		/*
		 * List<FelNode> children = node.getChildren(); StringBuilder code = new
		 * StringBuilder();
		 * 
		 * Iterator<FelNode> it = children.iterator(); FelNode first = null; if
		 * (it.hasNext()) { first = it.next(); FelMethod argMethod =
		 * first.toMethod(ctx); appendArg(code, argMethod); Class<?> t =
		 * argMethod.getReturnType(); // 将第一个参数的类型作为返回值的类型 type =
		 * t.isAssignableFrom(Number.class) ? t : String.class; } boolean
		 * hasNext = it.hasNext(); while (hasNext) { code.append("+"); FelNode n
		 * = it.next(); FelMethod argMethod = n.toMethod(ctx); appendArg(code,
		 * argMethod); hasNext = it.hasNext(); } return new FelMethod(type,
		 * code.toString());
		 */
		
		List<FelNode> children = node.getChildren();
		StringBuilder sb = new StringBuilder();
		FelNode right = null;
		if (children.size() == 2) {
			FelNode left = children.get(0);
			SourceBuilder lm = left.toMethod(ctx);
			appendArg(sb, lm,ctx,left);
			type = lm.returnType(ctx, left);
			right = children.get(1);
			sb.append("+");
		} else if (children.size() == 1) {
			right = children.get(0);
		}
		SourceBuilder rm = right.toMethod(ctx);
		if(Character.class.isAssignableFrom(rm.returnType(ctx, right))){
			type = rm.returnType(ctx, right);
		}
		appendArg(sb, rm,ctx,right);
		FelMethod m = new FelMethod(type, sb.toString());
		return m;

		/*
		 * for (FelNode n : children) { sb.append("(");
		 * sb.append(n.toJavaSrc(null)); sb.append(")"); sb.append("+"); }
		 * if(sb.length()>0){ sb.deleteCharAt(sb.length()-1); children.get(0) }
		 */
		// return sb.toString();
	}

	private void appendArg(StringBuilder sb, SourceBuilder argMethod,FelContext ctx,FelNode node) {
		Class<?> t = argMethod.returnType(ctx, node);
		sb.append("(");
		if (Number.class.isAssignableFrom(t)
				|| CharSequence.class.isAssignableFrom(t)) {
			// 数值型和字符型时，直接添加
			sb.append(argMethod.source(ctx, node));
		} else {
			sb.append("ObjectUtils.toString(").append(argMethod.source(ctx, node))
					.append(")");
		}
		sb.append(")");
	}

	/**
	 * 加法
	 * @param left
	 * @param right
	 * @return
	 */
	public static Object add(Object left, Object right){
		if(left == null || right == null){
			throw new NullPointerException("调用add()方法出错！,原因：当前参数为空");
		}
		try {
			if (left instanceof Object[]){
				left = NumberUtil.calArray(left);
			}
			if (right instanceof Object[]){
				right = NumberUtil.calArray(right);
			}
			if (left.equals("∞") || right.equals("∞"))
				return "∞";
			
			if (NumberUtil.isFloatingPointNumber(left) || NumberUtil.isFloatingPointNumber(right)) {
				double l = NumberUtil.toDouble(left);
				double r = NumberUtil.toDouble(right);
				return new Double(l + r);
			}
			
			if(left instanceof BigInteger && right instanceof BigInteger){
				BigInteger l = NumberUtil.toBigInteger(left);
				BigInteger r = NumberUtil.toBigInteger(right);
				return l.add(r);
			}
			
			if(left instanceof BigDecimal || right instanceof BigDecimal){
				BigDecimal l = NumberUtil.toBigDecimal(left);
				BigDecimal r = NumberUtil.toBigDecimal(right);
				return l.add(r);
			}
			
			if (left instanceof String && right instanceof Date) {
				return left + Add.DATE_FORMAT.format((Date) right);
			} else if (left instanceof Date && right instanceof String) {
				return Add.DATE_FORMAT.format((Date) left) + right;
			}
	
			BigInteger l = NumberUtil.toBigInteger(left);
			BigInteger r = NumberUtil.toBigInteger(right);
			BigInteger result = l.add(r);
			return NumberUtil.narrowBigInteger(left, right, result);
		} catch (Exception e) {
			return NumberUtil.toString(left).concat(NumberUtil.toString(right));
		}
	}

}
