package am.ik.yavi.runtime;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Adds a call to {@code am.ik.yavi.runtime.guest.Budget.tick()} to every method entry and
 * before every jump of the classes an example compiled to. Jump-based instrumentation
 * makes every loop iteration reachable for the budget, and method entries cover
 * recursion, without needing threads to interrupt a running program.
 */
final class Instrumenter {

	private static final String BUDGET = "am/ik/yavi/runtime/guest/Budget";

	private static final String TICK_DESCRIPTOR = "()V";

	private Instrumenter() {
	}

	/**
	 * Rewrites the given class file.
	 * @param classFile the class file bytes produced by javac
	 * @return the instrumented class file bytes
	 */
	static byte[] instrument(byte[] classFile) {
		ClassReader reader = new ClassReader(classFile);
		ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
		reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
					String[] exceptions) {
				MethodVisitor delegate = super.visitMethod(access, name, descriptor, signature, exceptions);
				return new MethodVisitor(Opcodes.ASM9, delegate) {
					@Override
					public void visitCode() {
						super.visitCode();
						visitTick();
					}

					@Override
					public void visitJumpInsn(int opcode, Label label) {
						visitTick();
						super.visitJumpInsn(opcode, label);
					}

					private void visitTick() {
						super.visitMethodInsn(Opcodes.INVOKESTATIC, BUDGET, "tick", TICK_DESCRIPTOR, false);
					}
				};
			}
		}, 0);
		return writer.toByteArray();
	}

}
