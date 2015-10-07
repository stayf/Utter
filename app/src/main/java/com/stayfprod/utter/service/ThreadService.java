package com.stayfprod.utter.service;

import android.os.Process;
import android.support.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class ThreadService {

    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static final int BACKGROUND_THREADS = NUMBER_OF_CORES > 1 ? NUMBER_OF_CORES - 1 : NUMBER_OF_CORES;
    private static final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    private static ExecutorService backgroundExecutor = Executors.newFixedThreadPool(BACKGROUND_THREADS, new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull final Runnable runnable) {
            return Executors.defaultThreadFactory().newThread(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(11);
                    runnable.run();
                }
            });
        }
    });
    private static ExecutorService audioExecutor = Executors.newFixedThreadPool(1, new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull final Runnable runnable) {
            return Executors.defaultThreadFactory().newThread(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
                    runnable.run();
                }
            });
        }
    });
    private static ExecutorService helpExecutor = Executors.newFixedThreadPool(1, new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull final Runnable runnable) {
            return Executors.defaultThreadFactory().newThread(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(19);
                    runnable.run();
                }
            });
        }
    });
    private static ExecutorService singleBackgroundExecutor = Executors.newFixedThreadPool(1, new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull final Runnable runnable) {
            return Executors.defaultThreadFactory().newThread(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(11);
                    runnable.run();
                }
            });
        }
    });
    private static ExecutorService chatExecutor = Executors.newFixedThreadPool(1, new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull final Runnable runnable) {
            return Executors.defaultThreadFactory().newThread(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(11);
                    runnable.run();
                }
            });
        }
    });
    private static ExecutorService intermediateExecutor = Executors.newFixedThreadPool(1, new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull final Runnable runnable) {
            return Executors.defaultThreadFactory().newThread(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(11);
                    runnable.run();
                }
            });
        }
    });
    private static ExecutorService voiceRecordExecutor = Executors.newFixedThreadPool(1, new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull final Runnable runnable) {
            return Executors.defaultThreadFactory().newThread(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                    runnable.run();
                }
            });
        }
    });
    private static ExecutorService audioHelpExecutor = Executors.newFixedThreadPool(1, new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull final Runnable runnable) {
            return Executors.defaultThreadFactory().newThread(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(11);
                    runnable.run();
                }
            });
        }
    });
    private static ExecutorService singleUserExecutor = Executors.newFixedThreadPool(1, new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull final Runnable runnable) {
            return Executors.defaultThreadFactory().newThread(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(10);
                    runnable.run();
                }
            });
        }
    });
    private static ExecutorService chatForegroundExecutor = Executors.newFixedThreadPool(1, new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull final Runnable runnable) {
            return Executors.defaultThreadFactory().newThread(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
                    runnable.run();
                }
            });
        }
    });

    public static void runSingleTaskUser(Runnable task) {
        singleUserExecutor.execute(task);
    }

    public static void runTaskBySchedule(Runnable task, long delay) {
        scheduledExecutor.schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    public static void runTaskAudioHelp(Runnable task) {
        audioHelpExecutor.execute(task);
    }

    public static void runTaskChatBackground(Runnable task) {
        chatExecutor.execute(task);
    }

    public static void runTaskChatForegroundExecutor(Runnable task) {
        chatForegroundExecutor.execute(task);
    }

    public static void runTaskIntermediateBackground(Runnable task) {
        intermediateExecutor.execute(task);
    }

    public static void runTaskBackground(Runnable task) {
        backgroundExecutor.execute(task);
    }

    public static void runSingleTaskAudio(Runnable task) {
        audioExecutor.execute(task);
    }

    public static void runSingleTaskVoiceRecord(Runnable task) {
        voiceRecordExecutor.execute(task);
    }

    public static void runSingleTaskWithLowestPriority(Runnable task) {
        helpExecutor.execute(task);
    }

    public static void runSingleTaskBackground(Runnable task) {
        singleBackgroundExecutor.execute(task);
    }

    public static void runConnectionCheckTask(Runnable task) {
        singleBackgroundExecutor.execute(task);
    }

}
