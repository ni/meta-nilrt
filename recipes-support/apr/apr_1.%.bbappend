# Enable support for pthreads so apache2 can find pthread_kill() in its configure step
CACHED_CONFIGUREVARS += "apr_cv_pthreads_cflags=-pthread apr_cv_process_shared_works=no"
