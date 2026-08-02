FROM nginx:alpine

# Copy static frontend assets directly to Nginx web root
COPY . /usr/share/nginx/html

# Remove the Dockerfile itself from served web assets
RUN rm -f /usr/share/nginx/html/Dockerfile

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost/ || exit 1

USER nginx
CMD ["nginx", "-g", "daemon off;"]
