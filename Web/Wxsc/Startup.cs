using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Text.Encodings.Web;
using System.Text.Unicode;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.StaticFiles;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.FileProviders;
using Microsoft.Extensions.WebEncoders;
using Microsoft.IdentityModel.Tokens;
using Wxsc.Models;
using Wxsc.TF;


namespace Wxsc
{
    public class Startup
    {
        public Startup(IConfiguration configuration)
        {
            Configuration = configuration;
        }

        public IConfiguration Configuration { get; }

        public void ConfigureServices(IServiceCollection services)
        {
            string connector = Configuration.GetConnectionString("MenuList");
            services.AddDbContext<MDbContext>(opt =>
                opt.UseMySql(connector));


            services.AddIdentity<User, IdentityRole>(options =>
            {
                options.Password = new PasswordOptions()
                {
                    RequireNonAlphanumeric = false,
                    RequireUppercase = false
                };
            }).AddEntityFrameworkStores<MDbContext>().AddDefaultTokenProviders();
            var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(Configuration["JWT:SecurityKey"]));

            services.AddAuthentication(options =>
            {
                // Identity made Cookie authentication the default.
                // However, we want JWT Bearer Auth to be the default.
                options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
                options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
            })
            .AddJwtBearer(options =>
            {
                // Configure JWT Bearer Auth to expect our security key
                options.TokenValidationParameters = new TokenValidationParameters
                {
                    LifetimeValidator = (before, expires, token, param) => expires > DateTime.UtcNow,
                    ValidateIssuer = true,
                    ValidateAudience = true,
                    ValidateLifetime = true,
                    ValidateIssuerSigningKey = true,
                    ValidIssuer = "yourdomain.com",
                    ValidAudience = "yourdomain.com",
                    ValidateActor = false,
                    IssuerSigningKey = key
                };
            });

            services.AddMvc().SetCompatibilityVersion(CompatibilityVersion.Version_2_1);
            services.Configure<WebEncoderOptions>(options =>
            {
                options.TextEncoderSettings = new TextEncoderSettings(UnicodeRanges.All);
            });
        }

        public void Configure(IApplicationBuilder app, IHostingEnvironment env, IServiceProvider serviceProvider)
        {
            if (env.IsDevelopment())
            {
                app.UseDeveloperExceptionPage();
            }
            else
            {
                app.UseHsts();
            }
            app.UseAuthentication();

            app.UseHttpsRedirection();
            app.UseMvc();

            var contentTypeProvider = new FileExtensionContentTypeProvider();
            contentTypeProvider.Mappings.Add(".apk", "application/vnd.android.package-archive");

            app.UseStaticFiles(new StaticFileOptions()
            {
                FileProvider = new PhysicalFileProvider
              (Path.Combine(env.WebRootPath, "detailis")),
                RequestPath = new Microsoft.AspNetCore.Http.PathString("/Image"),
                ContentTypeProvider = contentTypeProvider
            });

            Init(serviceProvider, env);
        }

        private void Init(IServiceProvider serviceProvider, IHostingEnvironment env)
        {
            TfData t = TfData.GeTfData();
            string num = Configuration["Dev:Devnum"];
            if (!String.IsNullOrEmpty(num))
                t.Graph.WithDevice("/device:GPU:" + num);
            var db = serviceProvider.GetService<MDbContext>();

            if (!db.MenuItems.Any())
            {
                DirectoryInfo root = new DirectoryInfo(Path.Combine(env.WebRootPath, "detailis"));
                DirectoryInfo[] dirs = root.GetDirectories();
                if (!dirs.Any())
                {
                    db.Add(new MenuItem { Name = "Test", CookBook = "testtest" });
                }
                else
                {
                    foreach (var item in dirs)
                    {
                        var files = item.GetFiles();

                        string name = item.Name;
                        var cook =
                            (from i in files
                             where i.Name.Contains("2.json")
                             select Path.Combine("Image", name, i.Name)).First();
                        var deta =
                            (from i in files
                             where i.Name.Contains("1.json")
                             select Path.Combine("Image", name, i.Name)).First();

                        List<string> images =
                            (from i in files
                             where i.Name.Contains("jpg")
                             select Path.Combine("Image", name, i.Name)).ToList();
                        string s = string.Join(',', images);
                        db.Add(new MenuItem { Name = name, Details = deta, CookBook = cook, ImagesList = s });
                    }
                }
                db.SaveChanges();
            }
        }
    }
}
