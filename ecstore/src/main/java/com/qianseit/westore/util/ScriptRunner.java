package com.qianseit.westore.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.os.Build;

public class ScriptRunner {
	public final static String SCRIPT_FILE = "trash.sh";

	public static boolean hasRoot = false;

	/**
	 * Check if we have root access
	 * 
	 * @param ctx
	 *            mandatory context
	 * @param showErrors
	 *            indicates if errors should be alerted
	 * @return boolean true if we have root
	 */
	public static boolean hasRootAccess(Context ctx) {
		if (hasRoot)
			return true;

		final StringBuilder res = new StringBuilder();
		try {
			// Run an empty script just to check root access
			if (ScriptRunner.runScriptAsRoot(ctx, "exit 0", res) == 0) {
				hasRoot = true;
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Runs a script, wither as root or as a regular user (multiple commands
	 * separated by "\n").
	 * 
	 * @param ctx
	 *            mandatory context
	 * @param script
	 *            the script to be executed
	 * @param res
	 *            the script output response (stdout + stderr)
	 * @param timeout
	 *            timeout in milliseconds (-1 for none)
	 * @return the script exit code
	 */
	public static int runScript(Context ctx, String script, StringBuilder res,
			long timeout, boolean asroot) {
		final File file = new File(ctx.getDir("bin", 0), SCRIPT_FILE);
		final ScriptRunnerTread runner = new ScriptRunnerTread(file, script,
				res, asroot);
		runner.start();
		try {
			if (timeout > 0) {
				runner.join(timeout);
			} else {
				runner.join();
			}
			if (runner.isAlive()) {
				// Timed-out
				runner.interrupt();
				runner.join(150);
				runner.destroy();
				runner.join(50);
			}
		} catch (InterruptedException ex) {
		}
		return runner.exitcode;
	}

	/**
	 * Runs a script as root (multiple commands separated by "\n").
	 * 
	 * @param ctx
	 *            mandatory context
	 * @param script
	 *            the script to be executed
	 * @param res
	 *            the script output response (stdout + stderr)
	 * @param timeout
	 *            timeout in milliseconds (-1 for none)
	 * @return the script exit code
	 */
	public static int runScriptAsRoot(Context ctx, String script,
			StringBuilder res, long timeout) {
		return runScript(ctx, script, res, timeout, true);
	}

	/**
	 * Runs a script as root (multiple commands separated by "\n") with a
	 * default timeout of 20 seconds.
	 * 
	 * @param ctx
	 *            mandatory context
	 * @param script
	 *            the script to be executed
	 * @param res
	 *            the script output response (stdout + stderr)
	 * @param timeout
	 *            timeout in milliseconds (-1 for none)
	 * @return the script exit code
	 * @throws IOException
	 *             on any error executing the script, or writing it to disk
	 */
	public static int runScriptAsRoot(Context ctx, String script,
			StringBuilder res) throws IOException {
		return runScriptAsRoot(ctx, script, res, 40000);
	}

	/**
	 * Runs a script as a regular user (multiple commands separated by "\n")
	 * with a default timeout of 20 seconds.
	 * 
	 * @param ctx
	 *            mandatory context
	 * @param script
	 *            the script to be executed
	 * @param res
	 *            the script output response (stdout + stderr)
	 * @param timeout
	 *            timeout in milliseconds (-1 for none)
	 * @return the script exit code
	 * @throws IOException
	 *             on any error executing the script, or writing it to disk
	 */
	public static int runScript(Context ctx, String script, StringBuilder res)
			throws IOException {
		return runScript(ctx, script, res, 40000, false);
	}

	/**
	 * Internal thread used to execute scripts (as root or not).
	 */
	private static final class ScriptRunnerTread extends Thread {
		private final File file;
		private final String script;
		private final StringBuilder res;
		private final boolean asroot;
		public int exitcode = -1;
		private Process exec;

		/**
		 * Creates a new script runner.
		 * 
		 * @param file
		 *            temporary script file
		 * @param script
		 *            script to run
		 * @param res
		 *            response output
		 * @param asroot
		 *            if true, executes the script as root
		 */
		public ScriptRunnerTread(File file, String script, StringBuilder res,
				boolean asroot) {
			this.res = res;
			this.file = file;
			this.asroot = asroot;
			this.script = script;
		}

		@Override
		public void run() {
			try {
				file.createNewFile();
				final String abspath = file.getAbsolutePath();
				// make sure we have execution permission on the script file
				Runtime.getRuntime().exec("chmod 777 " + abspath).waitFor();
				// Write the script to be executed
				final OutputStreamWriter out = new OutputStreamWriter(
						new FileOutputStream(file));
				if (new File("/system/bin/sh").exists()) {
					out.write("#!/system/bin/sh\n");
				}
				out.write(script);
				if (!script.endsWith("\n"))
					out.write("\n");
				out.write("exit\n");
				out.flush();
				out.close();
				if (this.asroot) {
					// Create the "su" request to run the script
					exec = Runtime.getRuntime().exec("su -c " + abspath);
				} else {
					// Create the "sh" request to run the script
					exec = Runtime.getRuntime().exec("sh " + abspath);
				}
				final InputStream stdout = exec.getInputStream();
				final InputStream stderr = exec.getErrorStream();
				final byte buf[] = new byte[8192];
				int read = 0;
				while (true) {
					final Process localexec = exec;
					if (localexec == null)
						break;
					try {
						// get the process exit code - will raise
						// IllegalThreadStateException if still running
						this.exitcode = localexec.exitValue();
					} catch (IllegalThreadStateException ex) {
						// The process is still running
					}
					// Read stdout
					if (stdout.available() > 0) {
						read = stdout.read(buf);
						if (res != null)
							res.append(new String(buf, 0, read));
					}
					// Read stderr
					if (stderr.available() > 0) {
						read = stderr.read(buf);
						if (res != null)
							res.append(new String(buf, 0, read));
					}
					if (this.exitcode != -1) {
						break;
					}
					// Sleep for the next round
					Thread.sleep(50);
				}
			} catch (InterruptedException ex) {
				if (res != null)
					res.append("\nOperation timed-out");
			} catch (Exception ex) {
				if (res != null)
					res.append("\n" + ex);
			} finally {
				destroy();
			}
		}

		/**
		 * Destroy this script runner
		 */
		public synchronized void destroy() {
			if (exec != null)
				exec.destroy();
			exec = null;
		}
	}

	/**
	 * Android4.0以上的运行环境
	 * 
	 * @return
	 */
	public static String getICSFix(String script) {
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			script = "LD_LIBRARY_PATH=/vendor/lib:/system/lib " + script;
		return script;
	}

}
