//
// Created by Administrator on 2017/5/17 0017.
//
#include "Core.h"

void loadConfTiny(char *conf);
void loadConfFmns(char *conf);
void loadTiny(char *str);
void loadFmns(char *str);
char *trimVal(char *src);
char *trim(char *src);

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
            strcpy(_first_h, trimVal(val));
        } else if (strcasecmp(trim(key), "https_ip") == 0) {
            strcpy(_host_s, trimVal(val));
        } else if (strcasecmp(trim(key), "https_port") == 0) {
            strcpy(_port_s, trimVal(val));
        } else if (strcasecmp(trim(key), "https_first") == 0) {
            strcpy(_first_s, trimVal(val));
        } else if (strcasecmp(trim(key), "http_del") == 0) {
            char *cl = "'|", *cr = "|'";
            char *inner_val, *inner_ptr = NULL;
            if ((inner_val = strtok_r(trimVal(val), ",", &inner_ptr)) != NULL) {
                strcpy(_del_h, cl);
                strcat(_del_h, trimVal(inner_val));
                strcat(_del_h, cr);
                while ((inner_val = strtok_r(NULL, ",", &inner_ptr)) != NULL) {
                    strcat(_del_h, cl);
                    strcat(_del_h, trimVal(inner_val));
                    strcat(_del_h, cr);
                }
            }
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
            strcpy(_first_h, trimVal(val));
        } else if (strcasecmp(trim(key), "https_ip") == 0) {
            strcpy(_host_s, trimVal(val));
        } else if (strcasecmp(trim(key), "https_port") == 0) {
            strcpy(_port_s, trimVal(val));
        } else if (strcasecmp(trim(key), "https_first") == 0) {
            strcpy(_first_s, trimVal(val));
        } else if (strcasecmp(trim(key), "http_del") == 0) {
            char *cl = "'|", *cr = "|'";
            char *inner_val, *inner_ptr = NULL;
            if ((inner_val = strtok_r(trimVal(val), ",", &inner_ptr)) != NULL) {
                strcpy(_del_h, cl);
                strcat(_del_h, trimVal(inner_val));
                strcat(_del_h, cr);
                while ((inner_val = strtok_r(NULL, ",", &inner_ptr)) != NULL) {
                    strcat(_del_h, cl);
                    strcat(_del_h, trimVal(inner_val));
                    strcat(_del_h, cr);
                }
            }
        }
    }
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