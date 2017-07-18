//
// Created by Administrator on 2017/5/17 0017.
//
#include "Core.h"

using namespace std;

string &trim(string &src);

string getHost(string &src);

void delHeader(string &src, string const &_ds);

int startWith(const char *src, const char *str);

void resFstLine(string &url, string &version);

string &trim(string &src) {
    if (src.empty()) return src;

    src.erase(0, src.find_first_not_of(" "));
    src.erase(src.find_last_not_of(" ") + 1);
    return src;
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
        if ((spos != mpos && (*(spos - 1) != '\n' || *(spos + 1) != ':')) &&
            (spos == mpos && *(spos + 1) != ':'))
            continue;
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
        _delHeader(src, _ds.substr(start, pos - start));
        start = pos + 1;
    }
    _delHeader(src, _ds.substr(start, len));

}