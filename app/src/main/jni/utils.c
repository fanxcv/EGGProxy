//
// Created by Administrator on 2017/5/17 0017.
//
#include "Core.h"
#include <ctype.h>

void loadConfTiny(char *conf);

void loadConfFmns(char *conf);

void loadTiny(char *str);

void loadFmns(char *str);

char *trimVal(char *src);

char *trim(char *src);

char *formatFirst(char *src);

char *getHost(char *str);

char *strlower(char *src);

char *delHeader(char *src, char *delstr);

int startWith(char *src, char *str);

void resFstLine(char *src, char **url, char **version);

char *replaceAll(const char *string, const char *substr, const char *replacement);

extern char _mode[16];
extern char _del_h[1024];
extern int _is_net, _all_https;
extern char _port_h[6], _port_s[6];
extern char _host_h[16], _host_s[16];
extern char _first_h[4096], _first_s[4096];

void loadConfTiny(char *conf) {

    char *p, *out_ptr = NULL;
    if ((p = strtok_r(conf, "\r\n", &out_ptr)) != NULL) {
        loadTiny(p);
        while ((p = strtok_r(NULL, "\r\n", &out_ptr)) != NULL) {
            loadTiny(p);
        }
    }

}

void loadConfFmns(char *conf) {

    char *p, *out_ptr = NULL;
    if ((p = strtok_r(conf, "\r\n", &out_ptr)) != NULL) {
        loadFmns(p);
        while ((p = strtok_r(NULL, "\r\n", &out_ptr)) != NULL) {
            loadFmns(p);
        }
    }

}

void loadTiny(char *str) {
    char s[strlen(str) + 1];
    strcpy(s, str);
    char *key, *val, *in_ptr = NULL;
    if ((key = strtok_r(s, "=\r\n", &in_ptr)) != NULL) {
        val = strtok_r(NULL, "=\r\n", &in_ptr);
        if (strcasecmp(trim(key), "mode") == 0) {
            strcpy(_mode, trimVal(val));
        } else if (strcasecmp(trim(key), "http_ip") == 0) {
            strcpy(_host_h, trimVal(val));
        } else if (strcasecmp(trim(key), "http_port") == 0) {
            strcpy(_port_h, trimVal(val));
        } else if (strcasecmp(trim(key), "http_first") == 0) {
            char *p = formatFirst(trimVal(val));
            strcpy(_first_h, p);
            free(p);
        } else if (strcasecmp(trim(key), "https_ip") == 0) {
            strcpy(_host_s, trimVal(val));
        } else if (strcasecmp(trim(key), "https_port") == 0) {
            strcpy(_port_s, trimVal(val));
        } else if (strcasecmp(trim(key), "https_first") == 0) {
            char *p = formatFirst(trimVal(val));
            strcpy(_first_s, p);
            free(p);
        } else if (strcasecmp(trim(key), "http_del") == 0) {
//            char *cl = "'|", *cr = "|'";
//            char *inner_val, *inner_ptr = NULL;
//            if ((inner_val = strtok_r(trimVal(val), ",", &inner_ptr)) != NULL) {
//                strcpy(_del_h, cl);
//                strcat(_del_h, strlower(trimVal(inner_val)));
//                strcat(_del_h, cr);
//                while ((inner_val = strtok_r(NULL, ",", &inner_ptr)) != NULL) {
//                    strcat(_del_h, cl);
//                    strcat(_del_h, strlower(trimVal(inner_val)));
//                    strcat(_del_h, cr);
//                }
//            }
            strcpy(_del_h, trimVal(val));
        }
    }
}

void loadFmns(char *str) {
    char s[strlen(str) + 1];
    strcpy(s, str);
    char *key, *val, *in_ptr = NULL;
    if ((key = strtok_r(s, "=\r\n", &in_ptr)) != NULL) {
        val = strtok_r(NULL, "=\r\n", &in_ptr);
        if (strcasecmp(trim(key), "mode") == 0) {
            strcpy(_mode, trimVal(val));
        } else if (strcasecmp(trim(key), "http_ip") == 0) {
            strcpy(_host_h, trimVal(val));
        } else if (strcasecmp(trim(key), "http_port") == 0) {
            strcpy(_port_h, trimVal(val));
        } else if (strcasecmp(trim(key), "http_first") == 0) {
            char *p = formatFirst(trimVal(val));
            strcpy(_first_h, p);
            free(p);
        } else if (strcasecmp(trim(key), "https_ip") == 0) {
            strcpy(_host_s, trimVal(val));
        } else if (strcasecmp(trim(key), "https_port") == 0) {
            strcpy(_port_s, trimVal(val));
        } else if (strcasecmp(trim(key), "https_first") == 0) {
            char *p = formatFirst(trimVal(val));
            strcpy(_first_s, p);
            free(p);
        } else if (strcasecmp(trim(key), "http_del") == 0) {
            char *cl = "'|", *cr = "|'";
            char *inner_val, *inner_ptr = NULL;
            if ((inner_val = strtok_r(trimVal(val), ",", &inner_ptr)) != NULL) {
                strcpy(_del_h, cl);
                strcat(_del_h, strlower(trimVal(inner_val)));
                strcat(_del_h, cr);
                while ((inner_val = strtok_r(NULL, ",", &inner_ptr)) != NULL) {
                    strcat(_del_h, cl);
                    strcat(_del_h, strlower(trimVal(inner_val)));
                    strcat(_del_h, cr);
                }
            }
        }
    }
}

char *strlower(char *src) {
    char s[strlen(src) + 1];
    strcpy(s, src);
    char *p = s;
    for (; *p != '\0'; p++)
        *p = (char) tolower(*p);
    return s;
}

char *formatFirst(char *src) {
    char *a, *b, *c, *d, *e, *f, *g, *h, *i, *j, *k;
    a = replaceAll(src, "[version]", "[V]");
    b = replaceAll(a, "[method]", "[M]");
    free(a);
    c = replaceAll(b, "[host]", "[H]");
    free(b);
    d = replaceAll(c, "[uri]", "[U]");
    free(c);
    e = replaceAll(d, "[MTD]", "[M]");
    free(d);
    f = replaceAll(e, "[Rr]", "\r");
    free(e);
    g = replaceAll(f, "[Nn]", "\n");
    free(f);
    h = replaceAll(g, "[Tt]", "\t");
    free(g);
    i = replaceAll(h, "\\r", "\r");
    free(h);
    j = replaceAll(i, "\\n", "\n");
    free(i);
    k = replaceAll(j, "\\t", "\t");
    free(j);

    return k;
}

char *trim(char *src) {
    char *ori_src = src;

    char *begin = src;
    char *end = src + strlen(src);

    if (begin == end) return ori_src;

    while (isblank(*begin))
        ++begin;

    while (isblank(*end) || !(*end))
        --end;

    if (begin > end) {
        *src = '\0';
        return ori_src;
    }

    while (begin != end) {
        *src++ = *begin++;
    }

    *src++ = *end;
    *src = '\0';

    return ori_src;
}

char *trimVal(char *src) {
    char *ori_src = src;

    char *begin = src;
    char *end = src + strlen(src);

    if (begin == end) return ori_src;

    while (isblank(*begin) || *begin == '"')
        ++begin;

    while (isblank(*end) || !(*end) || *end == '"' || *end == ';')
        --end;

    if (begin > end) {
        *src = '\0';
        return ori_src;
    }

    while (begin != end) {
        *src++ = *begin++;
    }

    *src++ = *end;
    *src = '\0';

    return ori_src;
}

char *replaceAll(const char *src, const char *findstr, const char *replacestr) {
    char *tok = NULL;
    char *newstr = NULL;
    char *oldstr = NULL;

    size_t findlen = strlen(findstr);
    size_t replacelen = strlen(replacestr);

    if (findstr == NULL || replacestr == NULL)
        return strdup(src);

    newstr = strdup(src);
    while ((tok = strstr(newstr, findstr))) {
        oldstr = newstr;
        newstr = malloc(strlen(oldstr) - findlen + replacelen + 1);
        if (newstr == NULL) {
            free(oldstr);
            return NULL;
        }
        memcpy(newstr, oldstr, tok - oldstr);
        memcpy(newstr + (tok - oldstr), replacestr, replacelen);
        memcpy(newstr + (tok - oldstr) + replacelen, tok + findlen,
               strlen(oldstr) - findlen - (tok - oldstr));
        memset(newstr + strlen(oldstr) - findlen + replacelen, 0, 1);

        free(oldstr);
    }

    return newstr;
}

void resFstLine(char *src, char **url, char **version) {
    char *fork = NULL;
    fork = strstr(trim(src), " ");
    size_t len = strlen(fork);
    *version = malloc(len);
    memcpy(*version, fork + 1, len - 1);
    memset(*version + (len - 1), 0, 1);
    if (startWith(src, "/")) {
        *url = malloc((fork - src) + 1);
        memcpy(*url, src, fork - src);
        memset(*url + (fork - src), 0, 1);
    } else {
        char *p = strstr(strstr(src, "://"), "/");
        if (p <= fork) {
            *url = malloc((fork - p) + 1);
            memcpy(*url, p, fork - src);
            memset(*url + (fork - p), 0, 1);
        }
    }

}

char *getHost(char *str) {
    char *fork = NULL, *host = NULL;
    if ((fork = strcasestr(str, "x-online-host"))) {
        unsigned long len = strstr(fork, "\r\n") - (fork + 14);
        host = malloc(len + 1);
        memcpy(host, fork + 14, len);
        memset(host + len, 0, 1);
    } else if ((fork = strcasestr(str, "host"))) {
        unsigned long len = strstr(fork, "\r\n") - (fork + 5);
        host = malloc(len + 1);
        memcpy(host, fork + 5, len);
        memset(host + len, 0, 1);
    }
    return host ? trim(host) : host;
}

int startWith(char *src, char *str) {
    for (; *src != '\0' && *str != '\0'; src++, str++)
        if (*src != *str) return 0;
    return 1;
}

char *delHeader(char *src, char *delstr) {
    char *inner_ptr, *ns = NULL;
    if (!delstr || strcmp(src, "\n\r\n") == 0) return src;
    char *del = strtok_r(delstr, ",", &inner_ptr);
    if (!del || strcmp(src, "\n\r\n") == 0) return src;
    size_t srclen = strlen(src);
    char *find = NULL, *fork = NULL;
    if ((find = strcasestr(src, del))) {
        if (*(find - 1) == '\n') {
            fork = strchr(find, '\r');
            size_t i = srclen - (fork - find) - 2;
            ns = malloc(i + 1);
            if ((find - 1) == src) {
                memcpy(ns, fork + 1, i);
            } else {
                memcpy(ns, src, find - 1 - src);
                memcpy(ns + (find - 1 - src), fork + 1, i - (find - 1 - src));
            }
            memset(ns + i, 0, 1);
            free(src);
        }
    } else {
        ns = src;
    }
    return delHeader(ns, inner_ptr);
}