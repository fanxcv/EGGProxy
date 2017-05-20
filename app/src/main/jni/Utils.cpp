//
// Created by Administrator on 2017/5/17 0017.
//
#include "Core.h"

using namespace std;

void loadConfTiny(const char *conf);

void loadTiny(char *str);

char *trimVal(char *src);

char *trim(char *src);

string &trim(string &src);

void formatFirst(string &src);

string getHost(string &src);

void delHeader(string &src, string const &_ds);

int startWith(const char *src, const char *str);

void resFstLine(string &url, string &version);

void replaceAll(string &src, string const &find, string const &replace);

extern int _is_net, _all_https;
extern string _mode, _del_h;
extern string _port_h, _port_s;
extern string _host_h, _host_s;
extern string _first_h, _first_s;

void loadConfTiny(const char *conf) {
    char *c = strdup(conf);
    char *p = NULL, *out_ptr = NULL;
    for ((p = strtok_r(c, "\n", &out_ptr)); p != NULL; (p = strtok_r(NULL, "\n", &out_ptr)))
        loadTiny(p);
    free(c);
}

void loadTiny(char *str) {
    char *key, *val, *in_ptr = NULL;
    if ((key = strtok_r(str, "=\r\n", &in_ptr)) != NULL) {
        val = strtok_r(NULL, "=\r\n", &in_ptr);
        if (strcasecmp(trim(key), "mode") == 0) {
            _mode = trimVal(val);
        } else if (strcasecmp(trim(key), "http_ip") == 0) {
            _host_h = trimVal(val);
        } else if (strcasecmp(trim(key), "http_port") == 0) {
            _port_h = trimVal(val);
        } else if (strcasecmp(trim(key), "http_first") == 0) {
            _first_h = trimVal(val);
            formatFirst(_first_h);
        } else if (strcasecmp(trim(key), "https_ip") == 0) {
            _host_s = trimVal(val);
        } else if (strcasecmp(trim(key), "https_port") == 0) {
            _port_s = trimVal(val);
        } else if (strcasecmp(trim(key), "https_first") == 0) {
            _first_s = trimVal(val);
            formatFirst(_first_s);
        } else if (strcasecmp(trim(key), "http_del") == 0) {
            _del_h = trimVal(val);
        }
    }
}

void formatFirst(string &src) {
    replaceAll(src, "[version]", "[V]");
    replaceAll(src, "[method]", "[M]");
    replaceAll(src, "[host]", "[H]");
    replaceAll(src, "[uri]", "[U]");
    replaceAll(src, "[MTD]", "[M]");
    replaceAll(src, "[Rr]", "\r");
    replaceAll(src, "[Nn]", "\n");
    replaceAll(src, "[Tt]", "\t");
    replaceAll(src, "\\r", "\r");
    replaceAll(src, "\\n", "\n");
    replaceAll(src, "\\t", "\t");
}

#define DEL(c) (isblank(c) || c == '\r')

char *trim(char *src) {
    char *ori_src = src;
    char *begin = src;
    char *end = src + strlen(src);
    if (begin == end) return ori_src;
    while (DEL(*begin)) ++begin;
    while (DEL(*end) || !(*end)) --end;
    if (begin > end) {
        *src = '\0';
        return ori_src;
    }
    while (begin != end) *src++ = *begin++;
    *src++ = *end;
    *src = '\0';
    return ori_src;
}

#define DELVAL(c) (isblank(c) || c == '\r' || c == '"')

char *trimVal(char *src) {
    char *ori_src = src;
    char *begin = src;
    char *end = src + strlen(src);
    if (begin == end) return ori_src;
    while (DELVAL(*begin)) ++begin;
    while (DELVAL(*end) || !(*end) || *end == ';') --end;
    if (begin > end) {
        *src = '\0';
        return ori_src;
    }
    while (begin != end) *src++ = *begin++;
    *src++ = *end;
    *src = '\0';
    return ori_src;
}

string &trim(string &src) {
    if (src.empty()) return src;

    src.erase(0, src.find_first_not_of(" "));
    src.erase(src.find_last_not_of(" ") + 1);
    return src;
}

void replaceAll(string &src, string const &find, string const &replace) {
    string::size_type pos = src.find(find), f_size = find.size(), r_size = replace.size();
    while (pos != string::npos) {
        src.replace(pos, f_size, replace);
        pos = src.find(find, pos + r_size);
    }
}

string getHost(string &src) {
    string ns;
    size_t start, len;
    char *fork = NULL;
    const char *pos = src.c_str();
    if ((fork = strcasestr(pos, "x-online-host"))) {
        len = strstr(fork, "\r\n") - (fork + 14);
        start = fork - pos + 14;
        ns = src.substr(start, len);
    } else if ((fork = strcasestr(pos, "host"))) {
        len = strstr(fork, "\r\n") - (fork + 5);
        start = fork - pos + 5;
        ns = src.substr(start, len);
    }
    return trim(ns);
}

void resFstLine(string &url, string &version) {
    trim(url);
    const char *p = url.c_str();
    size_t len = url.length(), pos = url.find(' ');
    if (pos == string::npos) return;
    version = url.substr(pos + 1, len - pos - 1);
    url.erase(pos);
    if (!startWith(url.c_str(), "/")) {
        size_t i = strstr(strstr(p, "://"), "/") - p;
        url.erase(0, i);
    }
}

int startWith(const char *src, const char *str) {
    for (; *src != '\0' && *str != '\0'; src++, str++)
        if (*src != *str) return 0;
    return 1;
}

#define NEN(c) (c != '\0' && c != '\r' && c != '\n')
#define INL(c) (c == '\r' || c == '\n')

void _delHeader(string &src, string const &delstr) {
    const char *mpos = src.c_str(), *del = delstr.c_str();
    char *spos = (char *) mpos;
    while (*spos) {
        spos = strcasestr(spos, del);
        if (!spos) break;
        if (spos != mpos && *(spos - 1) != '\n') continue;
        char *epos = spos;
        while NEN(*epos) epos++;
        while INL(*epos) epos++;
        src.erase(spos - mpos, epos - spos);
    }
}

void delHeader(string &src, string const &_ds) {
    if (src.empty() || _ds.empty()) return;
    size_t start = 0, pos = 0, len = _ds.length();
    while ((pos = _ds.find(',', pos + 1)) != string::npos) {
        _delHeader(src, _ds.substr(start, pos));
        start = pos + 1;
    }
    _delHeader(src, _ds.substr(start, len));

}