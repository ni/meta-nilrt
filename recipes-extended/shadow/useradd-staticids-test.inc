# Compares the passwd/group files that nilrt declares for usage with
# the 'useradd-staticids' extension
#
# Things we do here:
# - make sure that the passwd/group files remain ordered
#   (probably a test that does not belong here in shadow but we're here anyway)
# - make sure that the static assignments are outside the ranges defined
#   in login.defs so they do not conflict with on-target postinsts
#
# Note that this is tailored somewhat to nilrt usage and may require
# a bit work for generic upstream.

addtask check_useradd_staticids after do_install before do_package

python do_check_useradd_staticids() {

    def get_table_list(d, var, default):
        """Get the filenames of all passwd/group table files"""
        files = []
        bbpath = d.getVar('BBPATH')
        tables = d.getVar(var)
        if not tables:
            tables = default
        for conf_file in tables.split():
             files.append(bb.utils.which(bbpath, conf_file))
        return files

    def read_table_file(filename):
        """Read a passwd/group file and return ids and names"""
        last_name = None
        last_id = None
        id_table = {}

        bb.note("Reading table file at %s" % (filename))

        with open(filename, 'r') as f:
            for line in f:
                line = line.strip()
                if not line or line.startswith('#'):
                    continue
                fields = line.rstrip().split(':')
                if len(fields) < 2:
                    raise Exception("entry of '%s' has not enough fields in %s" % \
                        (line, filename))

                name = fields[0]
                try:
                    id = int(fields[2])
                except ValueError as e:
                    raise Exception("entry of '%s' in %s must have int ID field, " \
                        "not '%s'" % (line, filename, fields[2])) from e

                # We expect the ids in the files to be in descending order,
                # because historically we kept them in mostly-descending
                # order.
                if last_id != None and last_id < id:
                    bb.error("entries out of order: Entry '%s' (%d) has " \
                        "smaller ID than '%s' (%d) in %s and should be " \
                        "placed later in file." % \
                        (last_name, last_id, name, id, filename))
                    # once we have given one error for this id, we don't
                    # need to give any more
                    last_id = None
                    last_name = None
                else:
                    last_id = id
                    last_name = name

                if id not in id_table:
                    id_table[id] = (name, id)
                elif id_table[id][0] != name:
                    bb.error("Found duplicate entries for id %d: '%s' vs. '%s'" % \
                        (id, id_table[id][0], name))

        return id_table


    def read_login_defs(filename):
        """Read out UID/GID-related variables from logins.def"""

        # Default values of login.defs variables
        defs = {
            'UID_MIN': 1000,
            'UID_MAX': 60000,
            'SYS_UID_MIN': 101,
            'SYS_UID_MAX': 999,
            'GID_MIN': 1000,
            'GID_MAX': 60000,
            'SYS_GID_MIN': 101,
            'SYS_GID_MAX': 999,
        }

        bb.note("Reading defs file at %s" % (filename))

        with open(filename, 'r') as f:
            for line in f:
                line = line.strip()
                if not line or line.startswith('#'):
                    continue
                var, value = line.split(None, 1)
                if var in defs:
                    defs[var] = int(value)

        return defs


    def check_against_defs(d, id_var_name, default_file, defs):
        """Load either the UID or GID table, validate it, and make sure that
        nothing in them conflicts with the definitions that we have put into
        logins.def."""

        bb_useradd_table_var = 'USERADD_%s_TABLES' % (id_var_name)
        table_file = get_table_list(d, bb_useradd_table_var, default_file)
        if len(table_file) > 1:
            bb.error("%s is multiple files (%s) and this test doesn't yet " \
                "work with that" % (' '.join(bb_useradd_table_var), table_file))

        table = read_table_file(table_file[0])

        normal_id_min_var = '%s_MIN' % (id_var_name)
        normal_id_max_var = '%s_MAX' % (id_var_name)
        system_id_min_var = 'SYS_%s_MIN' % (id_var_name)
        system_id_max_var = 'SYS_%s_MAX' % (id_var_name)

        normal_id_range = range(defs[normal_id_min_var], \
                                defs[normal_id_max_var] + 1)
        system_id_range = range(defs[system_id_min_var], \
                                defs[system_id_max_var] + 1)

        for id in table.keys():
            if id in normal_id_range:
                bb.error("Static definition for %s (%d) is in dynamic range " \
                    "of %s (%d) to %s (%d) and may result in runtime conflicts" % \
                    (table[id][0], id, \
                    normal_id_min_var, defs[normal_id_min_var], \
                    normal_id_max_var, defs[normal_id_max_var]))
            if id in system_id_range:
                bb.error("Static definition for %s (%d) is in dynamic range " \
                    "of %s (%d) to %s (%d) and may result in runtime conflicts" % \
                    (table[id][0], id, \
                    system_id_min_var, defs[system_id_min_var], \
                    system_id_max_var, defs[system_id_max_var]))

    # sysconfdir is an absolute path so needs to be appended to D directly
    login_defs_filename = os.path.join(d.getVar('D') + d.getVar('sysconfdir'),
                                       'login.defs')
    defs = read_login_defs(login_defs_filename)

    check_against_defs(d, "UID", "files/passwd", defs)
    check_against_defs(d, "GID", "files/group", defs)
}

# Because this test depends on the UID/GID table files that come from
# outside the recipe tree, we need to mark them as being dependencies
# to trigger re-parsing when necessary.

python __anonymous() {
    bbpath = d.getVar('BBPATH')
    for varname, default in (('USERADD_UID_TABLES', 'files/passwd'),
                             ('USERADD_GID_TABLES', 'files/group')):
        tables = d.getVar(varname)
        if not tables:
            tables = default
        for conf_file in tables.split():
            bb.parse.mark_dependency(d, bb.utils.which(bbpath, conf_file))
}
