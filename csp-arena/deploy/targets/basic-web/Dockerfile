FROM nginx:alpine
COPY index.html /usr/share/nginx/html/index.html
COPY healthz /usr/share/nginx/html/healthz
COPY entrypoint.sh /docker-entrypoint-flag.sh
RUN chmod +x /docker-entrypoint-flag.sh
EXPOSE 80
CMD ["/docker-entrypoint-flag.sh"]
