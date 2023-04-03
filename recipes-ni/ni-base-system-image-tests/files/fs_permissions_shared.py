# Shared code between fs_permissions_diff and fs_permissions_known

import subprocess

class Logger:
    prefix_logs = []
    logs = []
    def log(self, log):
        self.logs.append(log)

    def prefix_log(self, log):
        self.prefix_logs.append(log)

    def report(self):
        for log in self.prefix_logs:
            print(log)
        for log in self.logs:
            print(log)

def run_cmd(cmd):
    output = subprocess.check_output(cmd)
    return output.decode('utf-8')

